package com.supersoft.oneapi.proxy.service;

import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingRequest;
import com.supersoft.oneapi.proxy.model.openai.EmbeddingResponse;

public interface OneapiEmbeddingService {
    /**
     * 是否适用
     * @return
     */
    boolean apply(OneapiProvider providerItem);

    /**
     * 具体执行embedding
     * @param request
     * @return
     */
    EmbeddingResponse embedding(EmbeddingRequest request,
                                OneapiProvider providerItem);
}
