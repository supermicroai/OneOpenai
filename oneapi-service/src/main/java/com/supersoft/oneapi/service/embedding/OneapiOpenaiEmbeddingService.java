package com.supersoft.oneapi.service.embedding;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingRequest;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingResponse;
import com.supersoft.oneapi.proxy.service.OneapiEmbeddingService;
import com.supersoft.oneapi.service.OneapiRequestService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 兼容标准协议的openai embedding
 */
@Service
public class OneapiOpenaiEmbeddingService implements OneapiEmbeddingService {
    String MODEL_NAME = "text-embedding-v3";

    @Resource
    OneapiRequestService requestService;

    /**
     * 目前特殊embedding只有ali的模型, 其他都是openai格式的接口
     * @param providerItem
     * @return
     */
    @Override
    public boolean apply(OneapiProvider providerItem) {
        if (providerItem == null) {
            return false;
        }
        return !MODEL_NAME.equals(providerItem.getModel());
    }

    @Override
    public EmbeddingResponse embedding(EmbeddingRequest request, OneapiProvider providerItem) {
        OneapiSingleResult<String> result = requestService.doRequest(providerItem, JSON.toJSONString(request));
        if (result == null || StringUtils.isBlank(result.getData())) {
            throw new RuntimeException("调用embedding失败");
        }
        return JSON.parseObject(result.getData(), EmbeddingResponse.class);
    }
}
