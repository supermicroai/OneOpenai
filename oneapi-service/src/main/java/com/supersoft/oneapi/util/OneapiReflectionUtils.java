package com.supersoft.oneapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OneapiReflectionUtils {
    private static final Map<String, Method> classMethodMap = new HashMap<>();

    public static Class getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        String key = clazz.getName() + "." + methodName;
        Method returnMethod = classMethodMap.get(key);
        if (returnMethod == null) {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    returnMethod = method;
                    classMethodMap.put(key, returnMethod);
                }
            }
        }
        return returnMethod;
    }

    /**
     * 获取所有该类的字段
     * 包括父类
     *
     * @param clazz
     * @return
     */
    public static List<Field> getDeclaredFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    public static Object parseFieldObject(String strVal, Field field) {
        Class clazz = field.getType();
        if (String.class.isAssignableFrom(clazz)) {
            return strVal;
        }
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
            Type[] types = pt.getActualTypeArguments();
            if (List.class.isAssignableFrom(clazz)) {
                Type listType = types[0];
                return JSON.parseArray(strVal, (Class) listType);
            } else if (Map.class.isAssignableFrom(clazz)) {
                return JSON.parseObject(strVal, new TypeReference<>(types[0], types[1]) {});
            }
        }
        return JSON.parseObject(strVal, clazz);
    }

    /**
     * 获取代理对象的真实对象
     *
     * @param proxy
     * @return
     * @throws Exception
     */
    public static Object getProxyTarget(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            //不是代理对象
            return proxy;
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else { //cglib
            return getCglibProxyTargetObject(proxy);
        }
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        return target;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        AopProxy aopProxy = (AopProxy) Proxy.getInvocationHandler(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
        return target;
    }

    public static Method getMethod(Class<?> clazz, String methodName, int targetCount) {
        String key = clazz.getName() + "." + methodName + "@" + targetCount;
        Method returnMethod = classMethodMap.get(key);
        if (returnMethod != null) {
            return returnMethod;
        }
        Method[] methods = clazz.getMethods();
        try {
            List<Method> filteredRealMethods = Arrays.stream(methods).filter(m -> {
                String name = m.getName();
                if (!name.equals(methodName)) {
                    return false;
                }
                int count = m.getParameterCount();
                return targetCount == count;
            }).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filteredRealMethods)) {
                log.error("找不到该方法{}，请检查该服务下是否有该方法", methodName);
                return null;
            }
            if (filteredRealMethods.size() > 1) {
                for (Method filteredRealMethod : filteredRealMethods) {
                    if (filteredRealMethod.getModifiers() == Modifier.PUBLIC) {
                        return filteredRealMethod;
                    }
                }
            }
            return filteredRealMethods.getFirst();
        } catch (Exception e) {
            log.error("获取方法失败", e);
            return null;
        }
    }
}
