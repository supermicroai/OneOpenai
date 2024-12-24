package com.supersoft.oneapi.service.embedding;

import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.service.OneapiEmbeddingService;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class OneapiEmbeddingServiceProxy {

    public static OneapiEmbeddingService of(OneapiProvider providerItem) {
        if (providerItem == null) {
            return null;
        }
        List<OneapiEmbeddingService> beans = OneapiServiceLocator.getBeansSafe(OneapiEmbeddingService.class);
        if (CollectionUtils.isEmpty(beans)) {
            return null;
        }
        return beans.stream()
                .filter(bean -> bean.apply(providerItem))
                .findFirst().orElse(null);
    }
}
