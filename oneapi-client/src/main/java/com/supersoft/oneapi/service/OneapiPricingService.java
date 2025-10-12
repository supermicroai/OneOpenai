package com.supersoft.oneapi.service;

import java.math.BigDecimal;

/**
 * 定价服务接口
 */
public interface OneapiPricingService {
    
    /**
     * 根据提供商、模型和请求/响应tokens数量计算成本
     * 
     * @param provider 提供商名称
     * @param model 模型名称
     * @param requestTokens 请求tokens数量
     * @param responseTokens 响应tokens数量
     * @return 计算出的成本
     */
    BigDecimal calculateCost(String provider, String model, Integer requestTokens, Integer responseTokens);
}