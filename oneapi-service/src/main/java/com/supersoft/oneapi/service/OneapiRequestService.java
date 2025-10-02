package com.supersoft.oneapi.service;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.common.exception.OneapiOutOfCreditException;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.provider.service.OneapiProviderService;
import com.supersoft.oneapi.proxy.model.openai.ChatResponse;
import com.supersoft.oneapi.token.data.OneapiTokenDO;
import com.supersoft.oneapi.token.service.OneapiTokenService;
import com.supersoft.oneapi.token.service.OneapiTokenCacheService;
import com.supersoft.oneapi.util.*;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Service
public class OneapiRequestService {
    private static final String SECRET_HEAD = "Bearer ";
    private static final int MAX_RETRY = 3;

    private static final CloseableHttpClient httpClient;
    public static final String PREFIX = "data: ";
    public static final String SEP = "\\r?\\n";

    static {
        // 创建连接管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1000); // 最大连接数
        connectionManager.setDefaultMaxPerRoute(200); // 每个路由最大连接数
        // 创建请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(120000)
                .setConnectionRequestTimeout(5000)
                .build();
        // 创建 HttpClient 实例
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Resource
    OneapiProviderService providerService;
    @Resource
    OneapiRequestLogService requestLogService;
    @Resource
    OneapiTokenService tokenService;
    @Resource
    OneapiTokenCacheService tokenCacheService;

    public OneapiSingleResult<String> doRequest(OneapiProvider provider,
                                                HttpServletResponse response, String jsonBody, String clientIp) {
        return doRequest(provider, response, jsonBody, true, clientIp);
    }

    public OneapiSingleResult<String> doRequest(OneapiProvider provider, String jsonBody, String clientIp) {
        return doRequest(provider, null, jsonBody, true, clientIp);
    }

    /**
     *
     * @param provider  api provider
     * @param response  target redirect stream
     * @param jsonBody  api request json
     * @param async
     * @param clientIp
     * @return
     */
    public OneapiSingleResult<String> doRequest(OneapiProvider provider,
                                        HttpServletResponse response,
                                        String jsonBody, boolean async, String clientIp) {
        if (OneapiCommonUtils.enableLog()) {
            log.info("选取提供节点: {}, 请求体: {}", JSON.toJSONString(provider, true), jsonBody);
        }
        HttpPost postRequest = new HttpPost(provider.getApi());
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        postRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + provider.getKey());
        // 开始转发并处理流返回逻辑
        long startTime = System.currentTimeMillis();
        try {
            postRequest.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(postRequest);
            if (response != null) {
                for (Header header : httpResponse.getAllHeaders()) {
                    response.addHeader(header.getName(), header.getValue());
                }
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                // 只要获取到流就认为调用成功了
                long duration = System.currentTimeMillis() - startTime;
                providerService.record(provider, duration, true);
                // 设置返回类型
                if (response != null) {
                    ContentType contentType = ContentType.get(entity);
                    if (contentType != null) {
                        response.setContentType(contentType.toString());
                        Charset charset = contentType.getCharset();
                        String charsetName = charset == null ? null : charset.name();
                        response.setCharacterEncoding(charsetName);
                    }
                }
                // 异步和同步处理返回值的方式不同
                String content;
                if (async && response != null) {
                    try (InputStream inputStream = entity.getContent();
                         ServletOutputStream outputStream = response.getOutputStream()) {
                        content = transferTo(inputStream, outputStream);
                    }
                } else {
                    content = EntityUtils.toString(entity);
                }
                // 异步处理减少对主线程的影响
                // 如果是无余额的错则需要更新余额到0
                // 如果没有找到分析服务则不阻塞
                // 同步处理便于处理异常
                if (StringUtils.isBlank(content)) {
                    throw new RuntimeException("服务返回为空");
                }
                // 处理请求异常
                String service = provider.getService();
                if (StringUtils.isNotBlank(service)) {
                    OneapiAccountService accountService =
                            OneapiServiceLocator.getBeanSafe(service, OneapiAccountService.class);
                    if (accountService != null) {
                        accountService.analysis(content);
                    }
                }
                // 记录调用
                requestLogService.requestLog(provider, content, null, clientIp);
                return OneapiSingleResult.success(content);
            }
            return OneapiSingleResult.fail("服务返回异常");
        } catch (Exception e) {
            log.error("转发openai调用异常", e);
            long duration = System.currentTimeMillis() - startTime;
            providerService.record(provider, duration, false);
            handleException(provider, e);
            requestLogService.requestLog(provider, jsonBody, e, clientIp);
            return OneapiSingleResult.fail("服务调用异常: " + e.getMessage());
        } finally {
            if (OneapiCommonUtils.enableLog()) {
                log.info("调用整体结束: {}, cost: {}", provider, System.currentTimeMillis() - startTime);
            }
        }
    }

    /**
     * 处理异常逻辑
     * @param e
     */
    private void handleException(OneapiProvider provider, Exception e) {
        // 余额不足
        if (e instanceof OneapiOutOfCreditException) {
            OneapiDingTalkUtils.sendAlert(String.format("账户余额不足, 服务提供者:%s, 账号:%s",
                    provider.getName(), provider.getNote()));
        }
    }

    public boolean checkApiKey(RequestEntity request, HttpServletResponse response) {
        String apiKey = request.getHeaders().getFirst("authorization");
        if (StringUtils.isBlank(apiKey)) {
            badRequest(response, "缺少授权信息");
            return true;
        }
        if (!apiKey.startsWith(SECRET_HEAD)) {
            badRequest(response, "apiKey格式异常");
            return true;
        }
        
        apiKey = apiKey.replace(SECRET_HEAD, StringUtils.EMPTY);
        
        // 首先尝试使用新的令牌缓存系统验证（性能优化）
        try {
            OneapiSingleResult<OneapiTokenDO> validateResult = tokenCacheService.validateApiKeyWithCache(apiKey);
            return validateResult != null && validateResult.isSuccess();
        } catch (Exception e) {
            log.warn("令牌缓存验证异常，尝试使用旧的验证方式: {}", e.getMessage());
        }
        return false;
    }

    public void badRequest(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write(message);
            response.getWriter().flush();
        } catch (Exception e) {
            log.error("Error occurred while writing response", e);
        }
    }

    private static boolean isErrorResponse(OneapiSingleResult<String> result) {
        return result == null || !result.isSuccess();
    }


    /**
     * 服务重试, 抛弃之前的节点选取另一个可用节点
     * @param response
     * @param model
     * @param supplier
     */
    public void invokeRetry(HttpServletResponse response, String model, Object request,
                             Function<OneapiProvider, OneapiSingleResult<String>> supplier) {
        Integer maxRetry = OneapiConfigUtils.getCacheConfigWithDef("oneapi.retry", MAX_RETRY);
        int count = 0;
        List<OneapiProvider> exclude = new ArrayList<>();
        OneapiSingleResult<String> invokeResult = null;
        // 统一改为json+utf8返回, 如果有特殊诉求后续在不同分支修改
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        while (isErrorResponse(invokeResult) && count++ < maxRetry) {
            if (invokeResult != null) {
                log.error("调用异常发生异常, 请求={}. 错误原因={}, 当前重试={}", request, invokeResult.getMessage(), count);
            }
            // 获取可用节点
            OneapiProvider providerItem = providerService.selectProvider(model, exclude);
            if (providerItem == null) {
                continue;
            }
            // 加入排除节点
            exclude.add(providerItem);
            invokeResult = supplier.apply(providerItem);
        }
        if (isErrorResponse(invokeResult)) {
            String error = invokeResult == null ? "调用异常, 不存在可用的节点" : "调用异常: " + invokeResult.getMessage();
            badRequest(response, error);
        }
    }



    /**
     * byte直接对拷
     * @param in
     * @param out
     * @throws IOException
     */
    public String transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "input stream is null");
        Objects.requireNonNull(out, "output stream is null");
        StringBuilder sb = new StringBuilder();
        ChatResponse response = null;
        long transferred = 0;
        byte[] buffer = new byte[8096];
        int read;
        while ((read = in.read(buffer, 0, 8096)) >= 0) {
            out.write(buffer, 0, read);
            out.flush();
            if (transferred < Long.MAX_VALUE) {
                try {
                    transferred = Math.addExact(transferred, read);
                } catch (ArithmeticException ignore) {
                    transferred = Long.MAX_VALUE;
                }
            }
            // 去除sse返回的固定前缀
            String stream = new String(buffer, 0, read);
            try {
                String[] lines = stream.split(SEP);
                for (String line : lines) {
                    if (line.startsWith(PREFIX)) {
                        line = line.substring(PREFIX.length());
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        ChatResponse chatResponse = ChatResponse.of(line);
                        if (chatResponse == null) {
                            continue;
                        }
                        response = chatResponse;
                        sb.append(chatResponse.getContent());
                    } else {
                        sb.append(line);
                    }
                }
            } catch (Exception e) {
                log.error("字符串解析失败: {}", stream, e);
            }
        }
        if (response != null) {
            response.setContent(sb.toString());
            return JSON.toJSONString(response);
        }
        return sb.toString();
    }
}
