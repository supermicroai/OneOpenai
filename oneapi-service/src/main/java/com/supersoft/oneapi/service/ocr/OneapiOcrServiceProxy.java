package com.supersoft.oneapi.service.ocr;

import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.service.OneapiOcrService;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class OneapiOcrServiceProxy {

    public static OneapiOcrService of(OneapiProvider providerItem) {
        if (providerItem == null) {
            return null;
        }
        List<OneapiOcrService> beans = OneapiServiceLocator.getBeansSafe(OneapiOcrService.class);
        if (CollectionUtils.isEmpty(beans)) {
            return null;
        }
        return beans.stream().filter(bean -> bean.apply(providerItem)).findFirst().orElse(null);
    }
}
