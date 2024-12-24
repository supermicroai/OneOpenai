package com.supersoft.oneapi.proxy.model.openai;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatChoice extends OneapiBaseObject {
    private final Integer index;
    private final ChatMessage message;
    private final ChatDelta delta;
    private final String finishReason;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
        private String name;
    }

    @Data
    public static class ChatDelta {
        private String role;
        private String content;
    }
}
