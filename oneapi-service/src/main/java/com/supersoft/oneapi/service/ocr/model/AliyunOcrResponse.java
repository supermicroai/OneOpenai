package com.supersoft.oneapi.service.ocr.model;

import lombok.Data;

import java.util.List;

@Data
public class AliyunOcrResponse {
    private String content;
    private List<AliyunOcrSubImage> prism_wordsInfo;
    private Long height;
    private Long width;
    private Long prism_wnum;

    @Data
    public static class AliyunOcrSubImage {
        private Long angle;
        private Long direction;
        private Long height;
        private Long width;
        private Long x;
        private Long y;
        private String word;
        private Long prob;
    }
}
