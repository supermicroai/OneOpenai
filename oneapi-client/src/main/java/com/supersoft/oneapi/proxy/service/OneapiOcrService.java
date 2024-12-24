package com.supersoft.oneapi.proxy.service;

import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.ocr.OcrRequest;
import com.supersoft.oneapi.proxy.model.ocr.OcrResponse;

public interface OneapiOcrService {
    /**
     * 是否适用
     * @return
     */
    boolean apply(OneapiProvider providerItem);

    /**
     * 具体执行ocr
     * @param request
     * @return
     */
    OcrResponse ocr(OcrRequest request, OneapiProvider providerItem);
}
