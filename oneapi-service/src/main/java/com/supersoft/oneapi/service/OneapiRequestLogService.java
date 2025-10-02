package com.supersoft.oneapi.service;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.openai.ChatResponse;
import com.supersoft.oneapi.proxy.model.openai.Usage;
import com.supersoft.oneapi.token.service.OneapiTokenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OneapiRequestLogService {
    
    @Resource
    private OneapiTokenService tokenService;

    /**
     * 记录用量
     * @param provider  服务提供者
     * @param result    返回
     * @param ex        异常
     * @param clientIp  客户端IP
     */
    public void requestLog(OneapiProvider provider, String result, Exception ex, String clientIp) {
        // 多线程减少日志打印带来的接口性能损失
        Thread.ofVirtual().start(() -> {
            if (provider == null) {
                return;
            }
            try {
                String providerName = provider.getName();
                String model = provider.getModel();
                int requestTokens = 0;
                int responseTokens = 0;
                Integer status = ex == null ? 1 : 0;
                String errorMsg = ex != null ? ex.getMessage() : null;

                // 从返回结果中提取token使用量
                if (StringUtils.isNotBlank(result) && ex == null) {
                    try {
                        // 尝试解析ChatResponse格式
                        ChatResponse chatResponse = JSON.parseObject(result, ChatResponse.class);
                        if (chatResponse != null && chatResponse.getUsage() != null) {
                            Usage usage = chatResponse.getUsage();
                            requestTokens = usage.getPromptTokens();
                            responseTokens = usage.getCompletionTokens();
                        }
                    } catch (Exception parseEx) {
                        log.debug("无法解析token使用量: {}", parseEx.getMessage());
                    }
                }

                // 记录到数据库
                tokenService.recordUsage(providerName, model, requestTokens, responseTokens,
                        status, errorMsg, clientIp != null ? clientIp : "unknown");

            } catch (Exception e) {
                log.error("记录使用情况到数据库失败", e);
            }
        });
    }

    /**
     * 从请求中提取模型名称
     */
    private String extractModelFromRequest(String request) {
        if (StringUtils.isBlank(request)) {
            return "unknown";
        }
        try {
            com.alibaba.fastjson.JSONObject jsonRequest = JSON.parseObject(request);
            if (jsonRequest != null && jsonRequest.containsKey("model")) {
                return jsonRequest.getString("model");
            }
        } catch (Exception e) {
            log.debug("无法从请求中提取模型名称: {}", e.getMessage());
        }
        return "unknown";
    }

    public static String maskString(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        int length = input.length();
        if (length <= 4) {
            return input;
        }
        int maxVisibleCharacters = Math.min(4, length / 3);
        String prefix = input.substring(0, maxVisibleCharacters);
        String suffix = input.substring(length - maxVisibleCharacters);
        // 中间的部分用*号替换
        int maskLength = length - prefix.length() - suffix.length();
        String maskedPart = "*".repeat(Math.min(5, maskLength));
        return prefix + maskedPart + suffix;
    }
}
