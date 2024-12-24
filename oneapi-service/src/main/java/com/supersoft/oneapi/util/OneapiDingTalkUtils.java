package com.supersoft.oneapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class OneapiDingTalkUtils {
    static final HttpClient httpclient;
    static final Map<String, RateLimiter> rateLimiterMap = new HashMap<>();
    static final Map<String, BlockingQueue<Message>> queueMap = new ConcurrentHashMap();

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        httpclient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy((httpResponse, httpContext) -> 10000).build();
    }

    /**
     * 改进原有client配置
     * @param message
     * @return
     * @throws IOException
     */
    public static SendResult sendText(Message message) throws IOException {
        String url = message.getUrl();
        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity se = new StringEntity(message.toTextMessage(), StandardCharsets.UTF_8);
        httpPost.setEntity(se);
        SendResult sendResult = new SendResult();
        HttpResponse response = httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            String result = EntityUtils.toString(response.getEntity());
            JSONObject obj = JSONObject.parseObject(result);
            Integer errCode = obj.getInteger("errcode");
            sendResult.setErrorCode(errCode);
            sendResult.setErrorMsg(obj.getString("errmsg"));
            sendResult.setSuccess(errCode.equals(0));
        } else {
            // 如果不为200则需要中断该链接
            httpPost.abort();
        }
        return sendResult;
    }

    public static void sendAlert(String message) {
        String url = OneapiConfigUtils.getConfig("oneapi.alert.ding", String.class);
        if (StringUtils.isBlank(url)) {
            return;
        }
        send(url, message);
    }

    public static void send(String url, String message) {
        send(url, message, null);
    }

    public static void send(String url, String message, List<String> atMobiles) {
        send(url, message, atMobiles, false);
    }

    public static void send(String url, String message, List<String> atMobiles, boolean isAtAll) {
        Message mosMessage = new Message();
        mosMessage.setText(message);
        mosMessage.setUrl(url);
        mosMessage.setAtMobiles(atMobiles);
        mosMessage.setAtAll(isAtAll);
        try {
            getQueue(url).add(mosMessage);
        } catch (Exception e) {
            log.error("ding talk queue full", e);
        }
    }

    private static BlockingQueue<Message> getQueue(String url) {
        return queueMap.computeIfAbsent(url, k -> {
            LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>(9999);
            // 为每一个队列启动一个死循环线程用于从队列中获取消息并发送
            Thread.ofVirtual().start(() -> {
                // 从队列获取消息并发送
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
                            log.error("send ding error: {}", sendResult);
                        }
                    } catch (Throwable e) {
                        log.error("poll ding failed", e);
                    }
                }
            });
            return queue;
        });
    }

    @Data
    public static class Message {
        String url;
        String text;
        boolean atAll;
        List<String> atMobiles;

        public String toTextMessage() {
            Map<String, Object> items = new HashMap();
            items.put("msgtype", "text");
            Map<String, String> textContent = new HashMap();
            if (StringUtils.isBlank(this.text)) {
                throw new IllegalArgumentException("text should not be blank");
            } else {
                textContent.put("content", this.text);
                items.put("text", textContent);
                Map<String, Object> atItems = new HashMap();
                if (this.atMobiles != null && !this.atMobiles.isEmpty()) {
                    atItems.put("atMobiles", this.atMobiles);
                }

                if (atAll) {
                    atItems.put("isAtAll", atAll);
                }

                items.put("at", atItems);
                return JSON.toJSONString(items);
            }
        }
    }

    @Data
    public static class SendResult {
        Integer errorCode;
        String errorMsg;
        boolean isSuccess;
    }
}
