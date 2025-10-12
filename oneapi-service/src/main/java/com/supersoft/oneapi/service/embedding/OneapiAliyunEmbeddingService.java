package com.supersoft.oneapi.service.embedding;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.service.embedding.model.AliEmbeddingRequest;
import com.supersoft.oneapi.service.embedding.model.AliEmbeddingResponse;
import com.supersoft.oneapi.proxy.model.openai.Embedding;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingRequest;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingResponse;
import com.supersoft.oneapi.proxy.service.OneapiEmbeddingService;
import com.supersoft.oneapi.service.OneapiRequestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 阿里云的embedding服务
 */
public class OneapiAliyunEmbeddingService implements OneapiEmbeddingService {
    String MODEL_NAME = "text-embedding-v3";

    @Resource
    OneapiRequestService requestService;

    @Override
    public boolean apply(OneapiProvider providerItem) {
        if (providerItem == null) {
            return false;
        }
        return MODEL_NAME.equals(providerItem.getModel());
    }

    @Override
    public EmbeddingResponse embedding(EmbeddingRequest request, OneapiProvider providerItem) {
        AliEmbeddingRequest aliRequest = new AliEmbeddingRequest();
        aliRequest.setModel("text-embedding-v3");
        AliEmbeddingRequest.AliEmbeddingInput embeddingInput = new AliEmbeddingRequest.AliEmbeddingInput();
        embeddingInput.setTexts(request.getInput());
        aliRequest.setInput(embeddingInput);
        OneapiSingleResult<String> result = requestService.doRequest(providerItem, JSON.toJSONString(aliRequest),
                request.getClientIp());
        String input = result.getData();
        AliEmbeddingResponse aliResponse = JSON.parseObject(input, AliEmbeddingResponse.class);
        EmbeddingResponse response = new EmbeddingResponse();
        response.setModel("text-embedding-v3");
        response.setObject("list");
        AliEmbeddingResponse.AliEmbeddingOutput output = aliResponse.getOutput();
        if (output != null) {
            List<AliEmbeddingResponse.AliEmbeddingOutputItem> embeddings = output.getEmbeddings();
            List<Embedding> list = embeddings.stream().map(item -> {
                Embedding embedding = new Embedding();
                embedding.setObject("embedding");
                embedding.setIndex(item.getTextIndex());
                embedding.setEmbedding(item.getEmbedding());
                return embedding;
            }).toList();
            response.setData(list);
        }
        response.setUsage(response.getUsage());
        return response;
    }
}
