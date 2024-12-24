package com.supersoft.oneapi.service.embedding.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.supersoft.oneapi.common.OneapiBaseObject;
import com.supersoft.oneapi.proxy.model.openai.Usage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AliEmbeddingResponse extends OneapiBaseObject {
    AliEmbeddingOutput output;
    Usage usage;
    @JSONField(name = "request_id")
    String requestId;

    @Data
    public static class AliEmbeddingOutput {
        List<AliEmbeddingOutputItem> embeddings;
    }

    @Data
    public static class AliEmbeddingOutputItem {
        @JSONField(name = "text_index")
        int textIndex;
        List<Double> embedding;
    }
}
