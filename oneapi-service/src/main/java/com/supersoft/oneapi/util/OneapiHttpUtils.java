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

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class OneapiHttpUtils {
    private static final CloseableHttpClient httpClient;
    private static final CookieStore cookieStore = new BasicCookieStore();

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
}
