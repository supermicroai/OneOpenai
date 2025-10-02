package com.supersoft.oneapi.util;

import com.alibaba.fastjson.JSON;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.http.RequestEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class OneapiHttpUtils {
    private static final CloseableHttpClient httpClient;
    private static final CookieStore cookieStore = new BasicCookieStore();
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    static {
        // 创建连接管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(200);
        // 创建请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(120000)
                .setConnectionRequestTimeout(3000)
                .build();
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static String post(String api, Object param) {
        return post(api, param, Map.of(), -1);
    }

    /**
     *
     * @param api
     * @param param
     * @param timeout 超时时间 单位为秒
     * @return
     */
    public static String post(String api, Object param, Map<String, String> headerMap, int timeout) {
        if (StringUtils.isBlank(api)) {
            throw new RuntimeException("Invalid param");
        }
        HttpPost request = new HttpPost(api);
        request.setHeader("Content-Type", "application/json");
        if (MapUtils.isNotEmpty(headerMap)) {
            headerMap.forEach(request::addHeader);
        }
        if (timeout > 0) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout * 1000)
                    .build();
            request.setConfig(requestConfig);
        }
        request.addHeader("Content-Type", "application/json");
        try {
            if (param != null) {
                String json = param instanceof String ? param.toString() : JSON.toJSONString(param);
                request.setEntity(new StringEntity(json));
            }
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
            // 关闭响应
            response.close();
        } catch (Exception e) {
            log.error("Failed to do post", e);
        }
        return null;
    }

    public static String get(String api, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(api);
        httpGet.setHeader("Content-Type", "application/json");
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(httpGet::addHeader);
        }
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = httpResponse.getEntity();
                if (responseEntity != null) {
                    return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                }
            }
        } catch(Exception e) {
            log.error("Failed to get, error={}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取客户端IP地址
     * 优先级：X-Forwarded-For > X-Real-IP > unknown
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(RequestEntity request) {
        if (request == null) {
            return "-";
        }
        
        // 优先从 X-Forwarded-For 头获取（代理/负载均衡场景）
        String xForwardedFor = request.getHeaders().getFirst(X_FORWARDED_FOR);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 从 X-Real-IP 头获取（Nginx等代理场景）
        String xRealIp = request.getHeaders().getFirst(X_REAL_IP);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(xRealIp)) {
            return xRealIp;
        }
        
        return "-";
    }
}
