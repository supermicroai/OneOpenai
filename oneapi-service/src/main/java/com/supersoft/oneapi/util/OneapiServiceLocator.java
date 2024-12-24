package com.supersoft.oneapi.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OneapiServiceLocator implements EnvironmentPostProcessor, BeanDefinitionRegistryPostProcessor,
        ApplicationContextAware, BeanFactoryAware, EnvironmentAware {
    @Getter
    private static ApplicationContext applicationContext;
    @Getter
    private static BeanFactory beanFactory;
    @Getter
    private static BeanDefinitionRegistry registry;
    private static Environment environment;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        if (OneapiServiceLocator.applicationContext == null) {
            OneapiServiceLocator.applicationContext = applicationContext;
        }
    }

    public static ConfigurableEnvironment getEnvironment() {
        if (environment instanceof ConfigurableEnvironment) {
            return (ConfigurableEnvironment) environment;
        }
        return null;
    }

    /**
     * 通过name获取 Bean
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过class获取Bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBeanSafe(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getBeanSafe(String name) {
        try {
            return applicationContext.getBean(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getBeanSafe(String name, Class<T> clazz) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取配置参数
     * @param key
     * @return
     */
    public static String getSpringProperty(String key) {
        return environment.getProperty(key);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static <T> List<T> getBeans(Class<T> clazz) {
        Map<String, T> beanMap = getBeanMap(clazz);
        if (MapUtils.isEmpty(beanMap)) {
            return null;
        }
        return new ArrayList<>(beanMap.values());
    }

    public static <T> List<T> getBeansSafe(Class<T> clazz) {
        Map<String, T> beanMap = getBeanMapSafe(clazz);
        if (MapUtils.isEmpty(beanMap)) {
            return null;
        }
        return new ArrayList<>(beanMap.values());
    }

    /**
     * 返回指定类型的所有实现
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeanMap(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz, Boolean.TRUE, Boolean.FALSE);
    }

    public static <T> Map<String, T> getBeanMapSafe(Class<T> clazz) {
        try {
            return applicationContext.getBeansOfType(clazz, Boolean.TRUE, Boolean.FALSE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 注册bean
     * @param name
     * @param bd
     */
    public static void registerBeanDefinition(String name, BeanDefinition bd) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        Object bean = OneapiServiceLocator.getBeanSafe(name);
        if (bean != null) {
            throw new RuntimeException("不允许替换已经存在bean");
        }
        beanFactory.registerBeanDefinition(name, bd);
    }

    /**
     * 注册bean
     * @param name
     * @param obj
     */
    public static void registerSingleton(String name, Object obj) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        Object bean = OneapiServiceLocator.getBeanSafe(name);
        if (bean != null) {
            throw new RuntimeException("不允许替换已经存在bean");
        }
        beanFactory.registerSingleton(name, obj);
    }

    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        beanFactory = factory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        OneapiServiceLocator.registry = registry;
    }

    /**
     * 尝试填充一个bean对象
     * @param bean
     * @param quiet
     */
    public static void autowiredBean(Object bean, boolean quiet) {
        ApplicationContext context = getApplicationContext();
        if (context != null) {
            try {
                AutowireCapableBeanFactory capableBeanFactory = context.getAutowireCapableBeanFactory();
                capableBeanFactory.autowireBean(bean);
                // 获取到所有的postProcessor然后分别执行
                Map<String, BeanPostProcessor> beans = getBeanMap(BeanPostProcessor.class);
                beans.forEach((beanName, beanPostProcessor) -> {
                    try {
                        beanPostProcessor.postProcessBeforeInitialization(bean, bean.getClass().getName());
                        beanPostProcessor.postProcessAfterInitialization(bean, bean.getClass().getName());
                    } catch (Exception e) {
                        log.error("执行自定义beanPostProcessor发生异常");
                    }
                });
            } catch (Throwable e) {
                log.error("填充类发生异常", e);
                if (!quiet) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void setEnvironment(Environment env) {
        environment = env;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 尽早加载该类，避免在其他地方使用时，该类还未加载
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

    }
}
