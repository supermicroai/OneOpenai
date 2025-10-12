package com.supersoft.oneapi.service.alert;

import java.util.List;

/**
 * 告警服务接口
 * 支持多种告警渠道：钉钉、Slack、邮件等
 */
public interface OneapiAlertService {
    
    /**
     * 发送告警消息
     * @param url 告警URL
     * @param message 告警消息内容
     */
    void sendAlert(String url, String message);
    
    /**
     * 发送告警消息（带@功能）
     * @param url 告警URL
     * @param message 告警消息内容
     * @param atUsers 需要@的用户列表
     */
    void sendAlert(String url, String message, List<String> atUsers);
    
    /**
     * 发送告警消息（带@所有人功能）
     * @param url 告警URL
     * @param message 告警消息内容
     * @param atUsers 需要@的用户列表
     * @param atAll 是否@所有人
     */
    void sendAlert(String url, String message, List<String> atUsers, boolean atAll);
    
    /**
     * 检查URL是否匹配此告警服务
     * @param url 告警URL
     * @return 是否匹配
     */
    boolean supports(String url);
    
    /**
     * 获取告警渠道类型
     * @return 告警渠道类型
     */
    String getAlertChannelType();
}