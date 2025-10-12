package com.supersoft.oneapi.provider.service.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.supersoft.oneapi.common.exception.OneapiOutOfCreditException;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import com.supersoft.oneapi.util.OneapiHttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OpenRouter余额查询服务
 * 通过API获取账户余额和使用情况
 */
@Slf4j
@Service("openRouterService")
public class OneapiOpenRouterService implements OneapiAccountService {
    private static final String ERROR = "max_tokens limit exceeded";
    // 新的API用于获取余额信息
    private static final String BALANCE_API = "https://openrouter.ai/api/v1/credits";

    @Override
    public Double getCredits(OneapiAccountDO account) {
        String apiKey = account.getApiKey();
        if (StringUtils.isBlank(apiKey)) {
            log.warn("获取OpenRouter余额信息失败，API Key为空");
            return null;
        }
        
        // 获取余额信息
        String result = OneapiHttpUtils.get(BALANCE_API, Map.of(AUTHORIZATION, BEARER + apiKey));
        if (StringUtils.isBlank(result)) {
            log.warn("获取OpenRouter余额信息失败，返回结果为空");
            return null;
        }
        
        try {
            OpenRouterCreditsResponse response = JSON.parseObject(result, OpenRouterCreditsResponse.class);
            if (response == null || response.getData() == null) {
                log.warn("解析OpenRouter余额信息失败，响应数据为空");
                return null;
            }
            
            OpenRouterCreditsData data = response.getData();
            
            // 设置已使用金额
            if (data.getTotalUsage() != null) {
                account.setCost(OneapiCommonUtils.shortDouble(data.getTotalUsage()));
            }

            // 设置余额并返回
            if (data.getTotalCredits() != null && data.getTotalUsage() != null) {
                // 余额 = 总信用额度 - 已使用额度
                double balance = data.getTotalCredits() - data.getTotalUsage();
                balance = OneapiCommonUtils.shortDouble(balance);
                account.setBalance(balance);
                return balance;
            }
            log.info("成功获取OpenRouter账户信息，总充值: {}, 已使用: {}", data.getTotalCredits(), data.getTotalUsage());
            return null;
        } catch (Exception e) {
            log.error("解析OpenRouter余额信息时发生异常", e);
            return null;
        }
    }

    @Override
    public void analysis(String result) {
        if (result.contains(ERROR)) {
            throw new OneapiOutOfCreditException("账户余额不足");
        }
    }

    @Data
    public static class OpenRouterCreditsResponse {
        OpenRouterCreditsData data;
    }

    @Data
    public static class OpenRouterCreditsData {
        @JSONField(name = "total_credits")
        Double totalCredits;
        @JSONField(name = "total_usage")
        Double totalUsage;
    }
}