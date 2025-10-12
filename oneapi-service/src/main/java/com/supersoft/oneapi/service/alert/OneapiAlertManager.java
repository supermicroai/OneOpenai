package com.supersoft.oneapi.service.alert;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.supersoft.oneapi.util.OneapiConfigUtils;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警管理器
 * 根据URL格式自动识别告警渠道类型
 */
@Slf4j
@Service
public class OneapiAlertManager {
    
    private static final String ALERT_URL_KEY = "oneapi.alert.url";

    @Resource
    List<OneapiAlertService> alertServices;

    /**
     * 发送告警消息
     * @param message 告警消息内容
     */
    public void sendAlert(String message) {
        sendAlert(message, null, false);
    }
    
    /**
     * 发送告警消息（带@功能）
     * @param message 告警消息内容
     * @param atUsers 需要@的用户列表
     */
    public void sendAlert(String message, List<String> atUsers) {
        sendAlert(message, atUsers, false);
    }
    
    /**
     * 发送告警消息（带@所有人功能）
     * @param message 告警消息内容
     * @param atUsers 需要@的用户列表
     * @param atAll 是否@所有人
     */
    public void sendAlert(String message, List<String> atUsers, boolean atAll) {
        try {
            String alertUrl = OneapiConfigUtils.getConfig(ALERT_URL_KEY, String.class);
            if (StringUtils.isBlank(alertUrl)) {
                log.warn("告警URL未配置，跳过发送告警: {}", message);
                return;
            }
            
            OneapiAlertService alertService = getAlertService(alertUrl);
            if (alertService != null) {
                alertService.sendAlert(alertUrl, message, atUsers, atAll);
                log.debug("告警消息已通过{}渠道发送: {}", alertService.getAlertChannelType(), message);
            } else {
                log.warn("未找到支持的告警服务，URL: {}, 消息: {}", alertUrl, message);
            }
        } catch (Exception e) {
            log.error("发送告警消息失败: {}", message, e);
        }
    }
    
    /**
     * 根据URL自动识别并获取告警服务实例
     * @param url 告警URL
     * @return 告警服务实例
     */
    private OneapiAlertService getAlertService(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        // 遍历所有服务，找到支持该URL的服务
        return alertServices.stream().filter(service -> service.supports(url))
                .findFirst().orElse(null);
    }
}