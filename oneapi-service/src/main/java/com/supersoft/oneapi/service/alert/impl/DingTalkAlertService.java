package com.supersoft.oneapi.service.alert.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.supersoft.oneapi.service.alert.OneapiAlertService;
import com.supersoft.oneapi.util.OneapiHttpClientUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 钉钉告警服务实现
 */
@Slf4j
@Service
public class DingTalkAlertService implements OneapiAlertService {
    
    private static final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    private static final Map<String, BlockingQueue<Message>> queueMap = new ConcurrentHashMap<>();
    
    // 钉钉机器人URL常量
    private static final String DINGTALK_ROBOT_URL_PREFIX = "https://oapi.dingtalk.com/robot/send?access_token=";
    
    // 匹配 ding://{token} 格式的正则表达式
    private static final Pattern DINGTALK_URL_PATTERN = Pattern.compile("ding://([^/]+)");
    
    @Override
    public void sendAlert(String url, String message) {
        sendAlert(url, message, null, false);
    }
    
    @Override
    public void sendAlert(String url, String message, List<String> atUsers) {
        sendAlert(url, message, atUsers, false);
    }
    
    @Override
    public void sendAlert(String url, String message, List<String> atUsers, boolean atAll) {
        if (StringUtils.isBlank(url)) {
            log.warn("钉钉告警URL为空");
            return;
        }
        
        // 解析URL中的token参数
        String actualUrl = url;
        String token = null;
        
        Matcher matcher = DINGTALK_URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            token = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
            // 构造实际的钉钉机器人URL
            actualUrl = DINGTALK_ROBOT_URL_PREFIX + token;
        }
        
        Message dingMessage = new Message();
        dingMessage.setText(message);
        dingMessage.setUrl(actualUrl);
        dingMessage.setAtMobiles(atUsers);
        dingMessage.setAtAll(atAll);
        
        try {
            getQueue(actualUrl).add(dingMessage);
        } catch (Exception e) {
            log.error("钉钉告警队列已满", e);
        }
    }
    
    @Override
    public boolean supports(String url) {
        // 支持ding://开头的URL格式
        return StringUtils.isNotBlank(url) && 
               (url.startsWith("https://oapi.dingtalk.com/robot/send?access_token=") || 
                url.startsWith("ding://"));
    }
    
    @Override
    public String getAlertChannelType() {
        return "dingtalk";
    }
    
    /**
     * 发送文本消息
     */
    private SendResult sendText(Message message) throws IOException {
        String url = message.getUrl();
        String jsonBody = message.toTextMessage();
        
        String response = OneapiHttpClientUtils.post(url, jsonBody);
        
        JSONObject obj = JSONObject.parseObject(response);
        Integer errCode = obj.getInteger("errcode");
        
        SendResult sendResult = new SendResult();
        sendResult.setErrorCode(errCode);
        sendResult.setErrorMsg(obj.getString("errmsg"));
        sendResult.setSuccess(errCode != null && errCode.equals(0));
        
        return sendResult;
    }
    
    /**
     * 获取消息队列
     */
    private BlockingQueue<Message> getQueue(String url) {
        return queueMap.computeIfAbsent(url, k -> {
            LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>(9999);
            // 为每一个队列启动一个死循环线程用于从队列中获取消息并发送
            Thread.ofVirtual().start(() -> {
                while (true) {
                    try {
                        Message message = queue.take();
                        // 不同的告警机器人rateLimiter分开
                        RateLimiter rateLimiter = rateLimiterMap.get(url);
                        if (rateLimiter == null) {
                            // 钉钉发送过快会导致302, 一分钟20条也就意味着3s一条
                            rateLimiter = RateLimiter.create(1);
                            rateLimiterMap.put(url, rateLimiter);
                        }
                        rateLimiter.acquire(3);
                        SendResult sendResult = sendText(message);
                        if (!sendResult.isSuccess()) {
                            log.error("发送钉钉消息失败: {}", sendResult);
                        }
                    } catch (Throwable e) {
                        log.error("处理钉钉消息队列失败", e);
                    }
                }
            });
            return queue;
        });
    }
    
    @Data
    public static class Message {
        private String url;
        private String text;
        private boolean atAll;
        private List<String> atMobiles;
        
        public String toTextMessage() {
            Map<String, Object> items = new HashMap<>();
            items.put("msgtype", "text");
            Map<String, String> textContent = new HashMap<>();
            if (StringUtils.isBlank(this.text)) {
                throw new IllegalArgumentException("text should not be blank");
            } else {
                textContent.put("content", this.text);
                items.put("text", textContent);
                Map<String, Object> atItems = new HashMap<>();
                if (this.atMobiles != null && !this.atMobiles.isEmpty()) {
                    atItems.put("atMobiles", this.atMobiles);
                }
                if (atAll) {
                    atItems.put("isAtAll", true);
                }
                items.put("at", atItems);
                return JSON.toJSONString(items);
            }
        }
    }
    
    @Data
    public static class SendResult {
        private Integer errorCode;
        private String errorMsg;
        private boolean isSuccess;
    }
}