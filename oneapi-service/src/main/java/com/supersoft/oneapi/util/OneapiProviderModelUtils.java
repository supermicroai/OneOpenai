package com.supersoft.oneapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.supersoft.oneapi.provider.model.ProviderModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务提供商模型配置工具类
 */
@Slf4j
public class OneapiProviderModelUtils {
    
    /**
     * 解析模型映射配置JSON
     * 通过parseProviderModelConfig实现，支持两种格式：
     * 1. 旧格式：{"model": "alias"}
     * 2. 新格式：{"model": {"alias": "alias", "inputPrice": 0.1, "outputPrice": 0.2}}
     *
     * @param modelsJson 模型配置JSON字符串
     * @return 模型映射映射
     */
    public static Map<String, String> parseModelMapping(String modelsJson) {
        if (StringUtils.isBlank(modelsJson)) {
            return Collections.emptyMap();
        }
        
        // 通过parseProviderModelConfig实现解析
        Map<String, ProviderModelConfig> providerModelConfig = parseProviderModelConfig(modelsJson);
        if (MapUtils.isEmpty(providerModelConfig)) {
            return Collections.emptyMap();
        }
        
        // 使用流的方式转换ProviderModelConfig映射为字符串映射（只取alias字段）
        return providerModelConfig.entrySet().stream()
                .filter(entry -> entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().getAlias()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAlias()));
    }
    
    /**
     * 解析提供商模型配置JSON
     * 支持两种格式：
     * 1. 旧格式：{"model": "alias"}
     * 2. 新格式：{"model": {"alias": "alias", "inputPrice": 0.1, "outputPrice": 0.2}}
     *
     * @param modelsJson 模型配置JSON字符串
     * @return 提供商模型配置映射
     */
    public static Map<String, ProviderModelConfig> parseProviderModelConfig(String modelsJson) {
        if (StringUtils.isBlank(modelsJson)) {
            return Collections.emptyMap();
        }
        
        try {
            // 首先尝试解析为新格式
            Map<String, ProviderModelConfig> newFormat = JSON.parseObject(modelsJson, new TypeReference<Map<String, ProviderModelConfig>>() {});
            if (newFormat != null) {
                return newFormat;
            }
        } catch (Exception e) {
            // 如果解析失败，尝试解析为旧格式并转换
            try {
                Map<String, String> oldFormat = JSON.parseObject(modelsJson, new TypeReference<Map<String, String>>() {});
                if (oldFormat != null) {
                    // 转换旧格式为新格式
                    Map<String, ProviderModelConfig> converted = new HashMap<>();
                    for (Map.Entry<String, String> entry : oldFormat.entrySet()) {
                        ProviderModelConfig config = new ProviderModelConfig();
                        config.setAlias(entry.getValue());
                        converted.put(entry.getKey(), config);
                    }
                    return converted;
                }
            } catch (Exception ex) {
                log.warn("无法解析模型配置JSON: {}", modelsJson, ex);
            }
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * 从提供商模型配置JSON中获取指定模型的配置信息
     * 
     * @param modelsJson 模型配置JSON字符串
     * @param model 模型名称
     * @return 指定模型的配置信息
     */
    public static ProviderModelConfig getModelConfig(String modelsJson, String model) {
        if (StringUtils.isBlank(modelsJson) || StringUtils.isBlank(model)) {
            return null;
        }
        
        // 解析提供商模型配置
        Map<String, ProviderModelConfig> providerModelConfig = parseProviderModelConfig(modelsJson);
        if (MapUtils.isEmpty(providerModelConfig)) {
            return null;
        }
        
        return providerModelConfig.get(model);
    }
    
    /**
     * 转换Map的键为小写
     *
     * @param map 模型配置映射
     * @return 转换后的映射
     */
    public static Map<String, String> toLowerCaseKeys(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            return map;
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
    }
    
    /**
     * 解析并处理提供商模型映射配置，直接返回指定模型的映射名称
     * 包含完整的解析、键转换和错误处理逻辑
     *
     * @param modelsJson 模型配置JSON字符串
     * @param modelName  模型名称
     * @return 指定模型的映射名称
     */
    public static String parseAndProcessModelMapping(String modelsJson, String modelName) {
        if (StringUtils.isBlank(modelsJson) || StringUtils.isBlank(modelName)) {
            return null;
        }
        try {
            // 解析模型映射配置
            Map<String, String> modelMap = parseModelMapping(modelsJson);
            if (MapUtils.isEmpty(modelMap)) {
                return null;
            }
            // 转换键为小写
            modelMap = toLowerCaseKeys(modelMap);
            // 返回指定模型的映射名称
            return modelMap.get(modelName.toLowerCase());
        } catch (Exception e) {
            log.warn("解析模型映射配置时出错: modelsJson={}, modelName={}", modelsJson, modelName, e);
            return null;
        }
    }
}