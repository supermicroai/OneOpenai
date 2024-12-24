package com.supersoft.oneapi.proxy.model.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@Data
@Slf4j
public class ChatResponse extends OpenAIResponse {
    String id;
    List<ChatChoice> choices;
    Integer created;
    @JSONField(name = "system_fingerprint")
    String fingerprint;
    String object;

    public static ChatResponse of(String response) {
        try {
            response = cutJson(response);
            return JSON.parseObject(response, ChatResponse.class);
        } catch (Exception e) {
            log.error("字符串解析失败: {}", response, e);
        }
        return null;
    }

    /**
     * 仅保留第一个{和最后一个}之间的字符串
     * @param json
     * @return
     */
    private static String cutJson(String json) {
        // 检查输入是否为null或空字符串
        if (StringUtils.isBlank(json)) {
            return json;
        }
        int startIndex = json.indexOf("{");
        int endIndex = json.lastIndexOf("}");
        // 检查是否找到了{和}
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            return StringUtils.EMPTY;
        }
        // 返回{和}之间的字符串，并包含它们
        return json.substring(startIndex, endIndex + 1);
    }

    public String getContent() {
        List<ChatChoice> choices = getChoices();
        if (CollectionUtils.isEmpty(choices)) {
            return StringUtils.EMPTY;
        }
        ChatChoice first = choices.getFirst();
        if (first == null) {
            return StringUtils.EMPTY;
        }
        ChatChoice.ChatDelta delta = first.getDelta();
        if (delta == null) {
            return StringUtils.EMPTY;
        }
        String content = delta.getContent();
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        return content;
    }

    public void setContent(String content) {
        List<ChatChoice> choices = getChoices();
        if (CollectionUtils.isEmpty(choices)) {
            return;
        }
        ChatChoice first = choices.getFirst();
        if (first == null) {
            return;
        }
        ChatChoice.ChatDelta delta = first.getDelta();
        if (delta == null) {
            return;
        }
        delta.setContent(content);
    }
}
