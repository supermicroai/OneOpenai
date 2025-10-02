package com.supersoft.oneapi.proxy.model.ocr;

import com.supersoft.oneapi.common.OneapiResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OcrRequest extends OneapiResult {
    String model;
    String url;
    String type;
    String clientIp;
}
