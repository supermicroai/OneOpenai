package com.supersoft.oneapi.proxy.model.ocr;

import lombok.Data;

import java.util.List;

@Data
public class OcrResponse {
    List<OcrBlock> blocks;
    String content;

    @Data
    public static class OcrBlock {
        /**
         * 内容
         */
        String content;
        /**
         * 角度
         */
        Long angle;
        /**
         * 置信度
         */
        Long confidence;
        /**
         * 方向
         */
        Long direction;
        /**
         * 高度
         */
        Long height;
        /**
         * 宽度
         */
        Long width;
        /**
         * x
         */
        Long x;
        /**
         * y
         */
        Long y;
    }
}
