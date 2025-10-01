package com.supersoft.oneapi.token.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 令牌使用记录数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oneapi_token_usage")
public class OneapiTokenUsageDO extends OneapiBaseObject {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 服务提供商
     */
    private String provider;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 请求令牌数
     */
    private Integer requestTokens;
    
    /**
     * 响应令牌数
     */
    private Integer responseTokens;
    
    /**
     * 成本
     */
    private BigDecimal cost;
    
    /**
     * 调用状态：1成功，0失败
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 客户端IP地址
     */
    private String ipAddress;
}
