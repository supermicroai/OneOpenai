package com.supersoft.oneapi.test;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.util.OneapiReflectionUtils;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TestExecutor {
    private String className;
    private String methodName;
    private final Map<String, Object> testInstanceCache = new HashMap<>();

    public void testClass() throws Exception {
        Class<?> testClz = Class.forName(className);
        Object testInstance = getTestInstance(testClz);
        runClassTest(testClz, testInstance);
    }

    public void testMethod(List<String> params) throws Exception {
        Class<?> testClz = Class.forName(className);
        Object testInstance = getTestInstance(testClz);
        //invoke test methodOO
        Method method = OneapiReflectionUtils.getMethod(testClz, methodName);
        invokeMethod(testInstance, method, params);
    }

    private Object getTestInstance(Class<?> testClz) throws Exception {
        if (testInstanceCache.get(className) != null) {
            return testInstanceCache.get(className);
        }
        Object testInstance = testClz.getConstructor().newInstance();
        OneapiServiceLocator.autowiredBean(testInstance, true);
        testInstanceCache.put(className, testInstance);
        return testInstance;
    }

    private void runClassTest(Class<?> testClz, Object testInstance) throws Exception {
        Method[] allMethods = testClz.getDeclaredMethods();
        List<Method> testMethods = new ArrayList<>();
        for (Method method : allMethods) {
            if (method.getName().startsWith("test")) {
                testMethods.add(method);
            }
        }
        //invoke test methods
        for (Method testMethod : testMethods) {
            invokeMethod(testInstance, testMethod);
        }
    }

    private static void invokeMethod(Object obj, Method method) throws Exception {
        invokeMethod(obj, method, null);
    }

    private static void invokeMethod(Object obj, Method method, List<String> params) throws Exception {
        if (method == null) {
            return;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        int paramSize = method.getParameterCount();
        if (paramSize > 0) {
            int size = parameterTypes.length;
            Object[] paramObjects = new Object[size];
            for (int i = 0; i < paramSize; i++) {
                if (i > size) {
                    paramObjects[i] = null;
                } else {
                    String param = params == null ? null : params.get(i);
                    paramObjects[i] = JSON.parseObject(param, parameterTypes[i]);
                }
            }
            method.invoke(obj, paramObjects);
        } else {
            method.invoke(obj);
        }
    }
}
