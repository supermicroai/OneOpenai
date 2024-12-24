package com.supersoft.oneapi.test;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class TestContainer {
    private static final String WELCOME_INPUT = "$Welcome$";
    private static final String REPEAT_INPUT = "r";
    private static TestExecutor testExecutor;
    private static boolean isInputValid = true;

    /**
     * TestsContainer must be within Spring Container
     */
    public static void start() {
        String input = WELCOME_INPUT;
        try {
            testExecutor = new TestExecutor();
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(
                    System.in));
            String lastInput = input;
            while (!(input).equalsIgnoreCase("quit")) {
                try {
                    isInputValid = true;
                    if (REPEAT_INPUT.equalsIgnoreCase(input)) {
                        input = lastInput;
                    }
                    //execute
                    execute(input);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (!REPEAT_INPUT.equalsIgnoreCase(input) && isInputValid) {
                        lastInput = input;
                    }
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            log.info("execute test command error: {} ...", input);
        }
    }

    private static void execute(String input) throws Exception {
        if (input == null || input.trim().length() == 0) {
            isInputValid = false;
            System.err.println("Your input is empty, please input a valid qualified name");
            return;
        }
        if (WELCOME_INPUT.equals(input)) {
            System.out.println("************** 欢迎使用轻量级TDD测试工具 ***************************");
            System.out.println("**** 1.测试单个方法，请在控制台输入方法全称");
            System.out.println("**** 支持eclipse格式, 例如：com.supersoft.lina.TestClass.test()");
            System.out.println("**** 支持idea格式, 例如：com.supersoft.lina.TestClass#test");
            System.out.println("**** 2.测试整个测试类，请在控制台输入类全称");
            System.out.println("**** 例如：com.supersoft.lina.TestClass.test");
            System.out.println("**** 3.重复上一次测试，只需在控制台输入字母 - ‘r’");
            System.out.println("***********************************************************************************");
            return;
        }
        if (!input.contains(".")) {
            isInputValid = false;
            System.err.println("Your input is not a valid qualified name");
            return;
        }
        boolean isMethod = false;
        if (isEclipseMethod(input) || isIdeaMethod(input)) {
            isMethod = true;
        }
        System.out.println("===Run " + (isMethod ? "Method" : "Class") + " start==== " + input);
        if (isMethod) {
            String className = null;
            String methodName = null;
            String [] parts = input.split(" ");
            input = parts[0];
            List<String> arguments = Arrays.asList(parts);
            // 构建参数
            if (arguments.size() > 1) {
                arguments = arguments.subList(1, arguments.size());
            } else {
                arguments = null;
            }
            if (isEclipseMethod(input)) {
                methodName = input.substring(input.lastIndexOf(".") + 1, input.indexOf("("));
                className = input.substring(0, input.lastIndexOf("."));
            }
            if (isIdeaMethod(input)) {
                methodName = input.substring(input.lastIndexOf("#") + 1);
                className = input.substring(0, input.lastIndexOf("#"));
            }
            if (methodName == null) {
                System.err.println("Your input " + input + " is not valid");
                return;
            }
            testExecutor.setClassName(className);
            testExecutor.setMethodName(methodName);
            testExecutor.testMethod(arguments);
        } else {
            testExecutor.setClassName(input);
            testExecutor.testClass();
        }
        System.out.println("===Run " + (isMethod ? "Method" : "Class") + " end====\n");
    }

    /**
     * @param input
     * @return
     */
    private static boolean isEclipseMethod(String input) {
        return input.indexOf("(") > 0;
    }

    private static boolean isIdeaMethod(String input) {
        return input.indexOf("#") > 0;//to accommodate idea
    }
}
