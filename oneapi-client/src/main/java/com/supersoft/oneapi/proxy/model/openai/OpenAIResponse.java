package com.supersoft.oneapi.proxy.model.openai;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OpenAIResponse extends OneapiBaseObject {
    String model;
    Usage usage;
}
