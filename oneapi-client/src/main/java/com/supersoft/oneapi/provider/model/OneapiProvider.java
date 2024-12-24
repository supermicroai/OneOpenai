package com.supersoft.oneapi.provider.model;

import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 展开之后的对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OneapiProvider extends OneapiBaseObject {
    public static final String LLM_TYPE = "llm";
    public static final String OCR_TYPE = "ocr";
    public static final String EMBEDDING_TYPE = "embedding";

    /**
     * 提供者来源
     */
    String name;
    /**
     * 提供者类型
     */
    String type;
    String note;
    /**
     * 提供者api
     */
    String api;
    /**
     * 标准模型名称
     */
    String model;
    /**
     * 模型映射名称
     */
    String modelMapping;
    /**
     * api key
     */
    String key;
    String ak;
    String sk;
    /**
     * 已付费
     */
    Double payment;
    /**
     * 剩余额度
     */
    Double balance;

    /**
     * 账号服务
     * 用于处理不同的服务提供者的错误码
     */
    String service;
}
