package com.supersoft.oneapi.service;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.openai.Usage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.supersoft.oneapi.provider.model.OneapiProvider.LLM_TYPE;

@Component
@Slf4j
public class OneapiRequestLogService {

    public void requestLog(long start, OneapiProvider provider, Object request, Exception e) {
        requestLog(start, provider, toString(request), null, e);
    }

    public void requestLog(long start, OneapiProvider provider, Object request, Object result) {
        requestLog(start, provider, toString(request), toString(result), null);
    }

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return JSON.toJSONString(obj);
    }

    /**
     * 记录用量
     * @param provider  服务提供者
     * @param request   请求
     * @param result    返回
     */
    private void requestLog(long start, OneapiProvider provider, String request, String result, Exception ex) {
        // 多线程减少日志打印带来的接口性能损失
        Thread.ofVirtual().start(() -> {
            if (provider == null) {
                return;
            }
            String type = provider.getType();
            OneapiRequestLog requestLog = new OneapiRequestLog();
            requestLog.setStart(start);
            requestLog.setDuration(System.currentTimeMillis() - start);
            String sk = maskString(provider.getSk());
            provider.setSk(sk);
            String apiKey = maskString(provider.getKey());
            provider.setKey(apiKey);
            requestLog.setProvider(provider);
            if (StringUtils.isNotBlank(request)) {
                requestLog.setRequest(JSON.parseObject(request));
            }
            if (StringUtils.isNotBlank(result) && LLM_TYPE.equals(type)) {
                requestLog.setResult(JSON.parseObject(result));
            }
            requestLog.setError(ex == null ? null : ex.getMessage());
            requestLog.setSuccess(ex == null);
            log.info(JSON.toJSONString(requestLog));
        });
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
        String prefix = input.substring(0, Math.min(maxVisibleCharacters, length));
        String suffix = input.substring(length - Math.min(maxVisibleCharacters, length));
        // 中间的部分用*号替换
        int maskLength = length - prefix.length() - suffix.length();
        String maskedPart = "*".repeat(Math.min(5, maskLength));
        return prefix + maskedPart + suffix;
    }

    @Data
    public static class OneapiRequestLog {
        Long start;
        Long duration;
        OneapiProvider provider;
        Object request;
        Object result;
        Usage usage;
        String error;
        Boolean success;
    }
}
