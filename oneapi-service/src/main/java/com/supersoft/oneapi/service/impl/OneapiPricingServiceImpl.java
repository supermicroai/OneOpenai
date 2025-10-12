package com.supersoft.oneapi.service.impl;

import com.supersoft.oneapi.provider.data.OneapiModelDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiModelMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.provider.model.ProviderModelConfig;
import com.supersoft.oneapi.service.OneapiPricingService;
import com.supersoft.oneapi.util.OneapiProviderModelUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 定价服务实现类
 */
@Service
@Slf4j
public class OneapiPricingServiceImpl implements OneapiPricingService {
    
    @Resource
    private OneapiModelMapper modelMapper;
    
    @Resource
    private OneapiProviderMapper providerMapper;
    
    /**
     * 根据提供商、模型和请求/响应tokens数量计算成本
     * 
     * @param provider 提供商名称
     * @param model 模型名称
     * @param requestTokens 请求tokens数量
     * @param responseTokens 响应tokens数量
     * @return 计算出的成本
     */
    @Override
    public BigDecimal calculateCost(String provider, String model, Integer requestTokens, Integer responseTokens) {
        // 默认价格为0
        BigDecimal cost = BigDecimal.ZERO;
        
        try {
            // 首先尝试从提供商模型配置中获取价格信息
            BigDecimal inputPrice = null;
            BigDecimal outputPrice = null;
            
            // 获取提供商信息
            OneapiProviderDO providerDO = providerMapper.selectByName(provider);
            if (providerDO != null && StringUtils.isNotBlank(providerDO.getModels())) {
                try {
                    // 使用工具类直接获取指定模型的配置信息
                    ProviderModelConfig modelConfig = OneapiProviderModelUtils.getModelConfig(providerDO.getModels(), model);
                    if (modelConfig != null) {
                        inputPrice = modelConfig.getInputPrice();
                        outputPrice = modelConfig.getOutputPrice();
                    }
                } catch (Exception e) {
                    log.warn("解析提供商模型配置时出错: provider={}, model={}", provider, model, e);
                }
            }
            
            // 如果提供商配置中没有价格信息，则使用模型表中的默认价格
            if (inputPrice == null || outputPrice == null) {
                OneapiModelDO modelDO = modelMapper.selectByVendorAndName(provider, model);
                if (modelDO != null) {
                    if (inputPrice == null && modelDO.getInputPrice() != null) {
                        inputPrice = modelDO.getInputPrice();
                    }
                    if (outputPrice == null && modelDO.getOutputPrice() != null) {
                        outputPrice = modelDO.getOutputPrice();
                    }
                }
            }
            
            // 如果找到了价格信息，则计算成本
            if (inputPrice != null && outputPrice != null) {
                // 计算输入和输出成本
                BigDecimal inputCost = inputPrice.multiply(BigDecimal.valueOf(requestTokens != null ? requestTokens : 0))
                        .divide(BigDecimal.valueOf(1000000), 10, RoundingMode.HALF_UP); // 按每1M个token计费
                
                BigDecimal outputCost = outputPrice.multiply(BigDecimal.valueOf(responseTokens != null ? responseTokens : 0))
                        .divide(BigDecimal.valueOf(1000000), 10, RoundingMode.HALF_UP); // 按每1M个token计费
                
                // 总成本
                cost = inputCost.add(outputCost);
            } else {
                log.warn("未找到模型定价信息: provider={}, model={}", provider, model);
            }
        } catch (Exception e) {
            log.error("计算成本时发生异常: provider={}, model={}", provider, model, e);
        }
        
        return cost;
    }
}