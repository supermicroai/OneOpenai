package com.supersoft.oneapi.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HttpClient工具类
 */
@Slf4j
public class OneapiHttpClientUtils {
    private static final CloseableHttpClient httpClient;
    
    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(20);
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy((httpResponse, httpContext) -> 10000).build();
    }
    
    /**
     * 发送POST请求
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param contentType 内容类型
     * @param authorization 认证信息（可选）
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String post(String url, String jsonBody, String contentType, String authorization) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(3000)
                .setSocketTimeout(10000).build();
        
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", contentType != null ? contentType : "application/json; charset=utf-8");
        
        if (authorization != null && !authorization.isEmpty()) {
            httpPost.addHeader("Authorization", authorization);
        }
        
        StringEntity se = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(se);
        
        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        
        if (statusCode != 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.error("HTTP请求失败，状态码: {}, 响应: {}", statusCode, responseBody);
            throw new RuntimeException("HTTP请求失败: " + statusCode);
        }
        
        return EntityUtils.toString(response.getEntity());
    }
    
    /**
     * 发送POST请求（默认JSON内容类型）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String post(String url, String jsonBody) throws IOException {
        return post(url, jsonBody, null, null);
    }
    
    /**
     * 发送POST请求（带认证信息）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param authorization 认证信息
     * @return 响应内容
     * @throws IOException IO异常
     */
    public static String postWithAuth(String url, String jsonBody, String authorization) throws IOException {
        return post(url, jsonBody, null, authorization);
    }
}