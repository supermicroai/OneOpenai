package com.supersoft.oneapi.proxy.model.openai;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Usage {
    @JSONField(name = "completion_tokens")
    int completionTokens;
    @JSONField(name = "prompt_tokens")
    int promptTokens;
    @JSONField(name = "total_tokens")
    int totalTokens;
}
