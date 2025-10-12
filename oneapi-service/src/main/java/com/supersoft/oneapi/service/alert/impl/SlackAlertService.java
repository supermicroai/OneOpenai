package com.supersoft.oneapi.service.alert.impl;

import com.alibaba.fastjson.JSON;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Slack告警服务实现
 */
@Slf4j
@Service
public class SlackAlertService implements OneapiAlertService {
    
    // Slack API URL常量
    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";
    
    // 匹配 slack://{token}/{channel} 格式的正则表达式
    private static final Pattern SLACK_URL_PATTERN = Pattern.compile("slack://([^/]+)/([^/]+)");
    
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
            log.warn("Slack Webhook URL为空");
            return;
        }
        
        try {
            // 解析URL中的token和channel参数
            String actualUrl = SLACK_API_URL;
            String token = null;
            String channel = null;
            
            Matcher matcher = SLACK_URL_PATTERN.matcher(url);
            if (matcher.matches()) {
                token = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
                channel = URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8);
            } else {
                // 如果不匹配新模式，保持原有逻辑
                actualUrl = url;
            }
            
            SlackMessage slackMessage = new SlackMessage();
            slackMessage.setText(message);
            slackMessage.setWebhookUrl(actualUrl);
            slackMessage.setToken(token);
            slackMessage.setChannel(channel);
            
            // 处理@用户
            if (atUsers != null && !atUsers.isEmpty()) {
                StringBuilder mentionText = new StringBuilder();
                for (String user : atUsers) {
                    mentionText.append("<@").append(user).append("> ");
                }
                slackMessage.setText(mentionText + message);
            } else if (atAll) {
                slackMessage.setText("<!channel> " + message);
            }
            
            sendSlackMessage(slackMessage);
            log.debug("Slack告警消息已发送: {}", message);
        } catch (Exception e) {
            log.error("发送Slack告警消息失败: {}", message, e);
        }
    }
    
    @Override
    public boolean supports(String url) {
        // 支持slack://开头的URL格式
        return StringUtils.isNotBlank(url) && url.startsWith("slack://");
    }
    
    @Override
    public String getAlertChannelType() {
        return "slack";
    }
    
    /**
     * 发送Slack消息
     */
    private void sendSlackMessage(SlackMessage message) throws IOException {
        String authorization = StringUtils.isNotBlank(message.getToken()) ? "Bearer " + message.getToken() : null;
        String jsonBody = message.toJsonMessage();
        
        OneapiHttpClientUtils.post(message.getWebhookUrl(), jsonBody, "application/json; charset=utf-8", authorization);
        log.debug("Slack消息已发送");
    }
    
    @Data
    public static class SlackMessage {
        private String webhookUrl;
        private String text;
        private String token;
        private String channel;
        
        public String toJsonMessage() {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", this.text);
            
            // 如果有channel参数，则添加到payload中
            if (StringUtils.isNotBlank(this.channel)) {
                payload.put("channel", this.channel);
            }
            
            return JSON.toJSONString(payload);
        }
    }
}