package com.supersoft.oneapi.service.embedding.model;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class AliEmbeddingRequest extends OneapiBaseObject {
    String model;
    AliEmbeddingInput input;
    Map<String, String> parameters;


    @Data
    public static class AliEmbeddingInput {
        List<String> texts;
    }
}
