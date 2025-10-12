package com.supersoft.oneapi.util;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.supersoft.oneapi.config.data.OneapiConfigDO;
import com.supersoft.oneapi.config.mapper.OneapiConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OneapiConfigUtils {
    private static final NullValueHolder NULL_VALUE_HOLDER = new NullValueHolder();
    private static final Cache<String, Object> configCache =
            CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

    public static String getStringCacheConfig(String key) {
        return getConfig(key, String.class);
    }

    public static <T> T getCacheConfig(String key, Class<T> clazz) {
        Object value = configCache.getIfPresent(key);
        if (value == null) {
            value = getConfig(key, clazz);
            value = value == null ? NULL_VALUE_HOLDER : value;
            configCache.put(key, value);
        }
        return value == NULL_VALUE_HOLDER ? null : (T) value;
    }

    public static <T> T getCacheConfigWithDef(String key, T defaultValue) {
        Object value = configCache.getIfPresent(key);
        if (value == null) {
            value = getConfigWithDef(key, defaultValue);
            value = value == null ? NULL_VALUE_HOLDER : value;
            configCache.put(key, value);
        }
        return value == NULL_VALUE_HOLDER ? null : (T) value;
    }

    /**
     * 暂时只能处理非复杂对象, 比如简单的Map, 不能处理Map中再嵌套复杂对象
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T getConfig(String key, Class<T> clazz) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        T value = getGroupConfig(null, key + "." + OneapiCommonUtils.getEnv(), clazz);
        if (value != null) {
            return value;
        }
        return getGroupConfig(null, key, clazz);
    }

    public static <T> T getConfigWithDef(String key, T defaultValue) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue can not be null");
        }
        T value = getGroupConfig(null, key + "." + OneapiCommonUtils.getEnv(),
                (Class<T>) defaultValue.getClass());
        if (value != null) {
            return value;
        }
        return getGroupConfig(null, key, defaultValue);
    }

    public static <T> T getGroupConfig(String group, String key, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue can not be null");
        }
        try {
            OneapiConfigDO one = getConfigDO(group, key);
            if (one == null) {
                return defaultValue;
            }
            if (defaultValue instanceof String) {
                return (T) one.getConfigValue();
            }
            return (T) JSON.parseObject(one.getConfigValue(), defaultValue.getClass());
        } catch (Exception e) {
            log.error("获取配置发生异常, group={}, key={}", group, key, e);
            return defaultValue;
        }
    }


    public static <T> T getGroupConfig(String group, String key, Class<T> clazz) {
        try {
            OneapiConfigDO one = getConfigDO(group, key);
            if (one == null) {
                return null;
            }
            try {
                if (clazz == String.class) {
                    return (T) one.getConfigValue();
                }
                return JSON.parseObject(one.getConfigValue(), clazz);
            } catch (Exception e) {
                return (T) one.getConfigValue();
            }
        } catch (Exception e) {
            log.error("获取配置发生异常, group={}, key={}", group, key, e);
            return null;
        }
    }

    public static void setConfig(String group, String key, Object value) {
        OneapiConfigDO one = getConfigDO(group, key);
        if (one == null) {
            one = new OneapiConfigDO();
            one.setConfigGroup(group);
            one.setConfigKey(key);
            one.setConfigValue(JSON.toJSONString(value));
            getMapper().insert(one);
        } else {
            one.setConfigValue(JSON.toJSONString(value));
            getMapper().updateById(one);
        }
    }

    private static OneapiConfigDO getConfigDO(String group, String key) {
        QueryWrapper<OneapiConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(group), "config_group", group);
        queryWrapper.eq("config_key", key);
        return getMapper().selectOne(queryWrapper);
    }

    private static OneapiConfigMapper getMapper() {
        return OneapiServiceLocator.getBean(OneapiConfigMapper.class);
    }

    public static class NullValueHolder {

    }
}
