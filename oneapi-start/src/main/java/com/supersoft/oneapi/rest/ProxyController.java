package com.supersoft.oneapi.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.proxy.model.ocr.OcrRequest;
import com.supersoft.oneapi.proxy.model.ocr.OcrResponse;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingRequest;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingResponse;
import com.supersoft.oneapi.proxy.service.OneapiEmbeddingService;
import com.supersoft.oneapi.proxy.service.OneapiOcrService;
import com.supersoft.oneapi.service.OneapiRequestService;
import com.supersoft.oneapi.service.embedding.OneapiEmbeddingServiceProxy;
import com.supersoft.oneapi.service.ocr.OneapiOcrServiceProxy;
import com.supersoft.oneapi.util.OneapiHttpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProxyController {

    @Resource
    OneapiRequestService requestService;

    @PostMapping("/v1/chat/completions")
    public void completions(RequestEntity<JSONObject> request, HttpServletResponse response) {
        if (request == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        Integer tokenId = requestService.extractTokenId(request, response);
        if (tokenId == null) {
            requestService.badRequest(response, "apiKey验证失败");
            return;
        }
        // 避免修改原始对象
        JSONObject requestBody = request.getBody();
        if (requestBody == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        String model = requestBody.getString("model");
        if (StringUtils.isBlank(model)) {
            requestService.badRequest(response, "模型不能为空");
            return;
        }
        // 从 HTTP 请求头获取客户端IP
        String clientIp = OneapiHttpUtils.getClientIpAddress(request);
        requestService.invokeRetry(response, model, requestBody, provider -> {
            // 设置tokenId到provider中
            provider.setTokenId(tokenId);
            String modelMapping = provider.getModelMapping();
            requestBody.put("model", modelMapping);
            String jsonBody = JSON.toJSONString(requestBody);
            String api = provider.getApi();
            provider.setApi(api + "/chat/completions");
            return requestService.doRequest(provider, response, jsonBody, clientIp);
        });
    }

    @PostMapping("/v1/ocr")
    @ResponseBody
    public void ocr(RequestEntity<OcrRequest> request, HttpServletResponse response) {
        if (request == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        Integer tokenId = requestService.extractTokenId(request, response);
        if (tokenId == null) {
            requestService.badRequest(response, "apiKey验证失败");
            return;
        }
        OcrRequest ocrRequest = request.getBody();
        if (ocrRequest == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        String model = ocrRequest.getModel();
        if (StringUtils.isBlank(model)) {
            requestService.badRequest(response, "模型不能为空");
            return;
        }
        // 从 HTTP 请求头获取客户端IP
        String clientIp = OneapiHttpUtils.getClientIpAddress(request);
        requestService.invokeRetry(response, model, ocrRequest, provider -> {
            // 设置tokenId到provider中
            provider.setTokenId(tokenId);
            String modelMapping = provider.getModelMapping();
            ocrRequest.setModel(modelMapping);
            ocrRequest.setClientIp(clientIp);
            OneapiOcrService ocrService = OneapiOcrServiceProxy.of(provider);
            if (ocrService == null) {
                return OneapiSingleResult.fail("未找到可用的ocr服务实现");
            }
            try {
                OcrResponse ocrResponse = ocrService.ocr(ocrRequest, provider);
                if (ocrResponse == null) {
                    return OneapiSingleResult.fail("服务调用异常");
                }
                response.getWriter().write(JSON.toJSONString(ocrResponse));
                response.getWriter().flush();
                return OneapiSingleResult.success();
            } catch (Exception e) {
                log.error("Error occurred while writing response", e);
                return OneapiSingleResult.fail("服务调用异常: " + e.getMessage());
            }
        });
    }

    @PostMapping("/v1/embeddings")
    public void embeddings(RequestEntity<EmbeddingRequest> request, HttpServletResponse response) {
        if (request == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        Integer tokenId = requestService.extractTokenId(request, response);
        if (tokenId == null) {
            requestService.badRequest(response, "apiKey验证失败");
            return;
        }
        EmbeddingRequest embeddingRequest = request.getBody();
        if (embeddingRequest == null) {
            requestService.badRequest(response, "入参不能为空");
            return;
        }
        String model = embeddingRequest.getModel();
        if (StringUtils.isBlank(model)) {
            requestService.badRequest(response, "模型不能为空");
            return;
        }
        // 从 HTTP 请求头获取客户端IP
        String clientIp = OneapiHttpUtils.getClientIpAddress(request);
        // 设置客户端IP到请求对象中
        embeddingRequest.setClientIp(clientIp);
        requestService.invokeRetry(response, model, embeddingRequest, provider -> {
            // 设置tokenId到provider中
            provider.setTokenId(tokenId);
            OneapiEmbeddingService embeddingService = OneapiEmbeddingServiceProxy.of(provider);
            if (embeddingService == null) {
                return OneapiSingleResult.fail("未找到可用的embedding服务实现");
            }
            EmbeddingResponse embeddingResponse = embeddingService.embedding(embeddingRequest, provider);
            try {
                response.getWriter().write(JSON.toJSONString(embeddingResponse));
                response.getWriter().flush();
                return OneapiSingleResult.success();
            } catch (Exception e) {
                log.error("Error occurred while writing response", e);
                return OneapiSingleResult.fail("服务调用异常: " + e.getMessage());
            }
        });
    }


}
