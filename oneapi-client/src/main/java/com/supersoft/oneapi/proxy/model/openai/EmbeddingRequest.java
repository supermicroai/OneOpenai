package com.supersoft.oneapi.proxy.model.openai;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmbeddingRequest extends OneapiBaseObject {
    List<String> input;
    String model;
}
