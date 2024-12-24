package com.supersoft.oneapi.proxy.model.openai;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmbeddingResponse extends OpenAIResponse {
    String object;
    List<Embedding> data;
    String model;
    Usage usage;
}
