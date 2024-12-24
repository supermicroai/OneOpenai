package com.supersoft.oneapi.provider.service;

import com.supersoft.oneapi.provider.model.OneapiProvider;

import java.util.List;
import java.util.Set;

public interface OneapiProviderService {
    /**
     * 根据模型获取一个当前的提供者
     * @param modelName
     * @return
     */
    OneapiProvider selectProvider(String modelName, List<OneapiProvider> exclude);

    /**
     * 指定提供者和模型获取一个当前的提供者
     * @param provider
     * @param modelName
     * @return
     */
    OneapiProvider selectProvider(String provider, String modelName, List<OneapiProvider> exclude);

    /**
     * 调用结束
     * @param item
     */
    void record(OneapiProvider item, long duration, Boolean success);
}
