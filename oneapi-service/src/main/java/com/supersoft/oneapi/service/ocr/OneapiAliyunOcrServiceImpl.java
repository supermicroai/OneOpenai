package com.supersoft.oneapi.service.ocr;

import com.alibaba.fastjson.JSON;
import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeGeneralRequest;
import com.aliyun.ocr_api20210707.models.RecognizeGeneralResponse;
import com.aliyun.teaopenapi.models.Config;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.proxy.model.ocr.OcrRequest;
import com.supersoft.oneapi.proxy.model.ocr.OcrResponse;
import com.supersoft.oneapi.proxy.service.OneapiOcrService;
import com.supersoft.oneapi.service.OneapiRequestLogService;
import com.supersoft.oneapi.service.ocr.model.AliyunOcrResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供阿里云的通用OCR服务
 */
@Slf4j
@Service
public class OneapiAliyunOcrServiceImpl implements OneapiOcrService {
    private static final Map<String, Client> ocrClientMap = new ConcurrentHashMap<>();

    @Resource
    OneapiRequestLogService requestLogService;
    /**
     * 构建ocr的client
     * @param providerItem
     * @return
     */
    private Client getOcrClient(OneapiProvider providerItem) {
        String key = JSON.toJSONString(providerItem);
        return ocrClientMap.computeIfAbsent(key, k -> {
            try {
                String ak = providerItem.getAk();
                String sk = providerItem.getSk();
                Config config = new Config().setAccessKeyId(ak).setAccessKeySecret(sk);
                config.endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";
                config.setConnectTimeout(30);
                return new Client(config);
            } catch (Exception e) {
                log.error("创建阿里云OCR客户端失败", e);
            }
            return null;
        });
    }

    /**
     * OCR service is only provided by Aliyun
     * Apply to all providers as there's only one implementation
     * @param providerItem
     * @return
     */
    @Override
    public boolean apply(OneapiProvider providerItem) {
        return providerItem != null;
    }

    @Override
    public OcrResponse ocr(OcrRequest request, OneapiProvider providerItem) {
        String url = request.getUrl();
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String clientIp = request.getClientIp();
        try {
            RecognizeGeneralRequest generalRequest = new RecognizeGeneralRequest();
            generalRequest.setBody(getInputStreamFromUrl(url));
            RecognizeGeneralResponse resp = getOcrClient(providerItem).recognizeGeneral(generalRequest);
            if (resp != null && resp.getBody() != null) {
                String data = resp.getBody().getData();
                AliyunOcrResponse ocrResponse = JSON.parseObject(data, AliyunOcrResponse.class);
                if (ocrResponse == null) {
                    return null;
                }
                OcrResponse result = new OcrResponse();
                List<AliyunOcrResponse.AliyunOcrSubImage> words = ocrResponse.getPrism_wordsInfo();
                if (CollectionUtils.isNotEmpty(words)) {
                    List<OcrResponse.OcrBlock> list = words.stream().map(item -> {
                        OcrResponse.OcrBlock ocrBlock = new OcrResponse.OcrBlock();
                        ocrBlock.setContent(item.getWord());
                        ocrBlock.setConfidence(item.getProb());
                        ocrBlock.setAngle(item.getAngle());
                        ocrBlock.setDirection(item.getDirection());
                        ocrBlock.setHeight(item.getHeight());
                        ocrBlock.setWidth(item.getWidth());
                        ocrBlock.setX(item.getX());
                        ocrBlock.setY(item.getY());
                        return ocrBlock;
                    }).toList();
                    result.setBlocks(list);
                }
                result.setContent(ocrResponse.getContent());
                requestLogService.requestLog(providerItem, JSON.toJSONString(result), null, clientIp);
                return result;
            }
        } catch (Exception e) {
            log.error("ocr error", e);
            requestLogService.requestLog(providerItem, null, e, clientIp);
        }
        return null;
    }

    public static InputStream getInputStreamFromUrl(String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        }
        throw new RuntimeException("获取输入流失败");
    }
}
