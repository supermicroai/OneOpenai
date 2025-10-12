package com.supersoft.oneapi.provider.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务提供商模型配置
 * 用于存储模型的别名映射和价格信息
 */
@Data
public class ProviderModelConfig {
    /**
     * 模型别名映射
     */
    private String alias;
    
    /**
     * 输入token价格（每1M个token）
     */
    private BigDecimal inputPrice;
    
    /**
     * 输出token价格（每1M个token）
     */
    private BigDecimal outputPrice;
}