package com.supersoft.oneapi.provider.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("oneapi_provider")
public class OneapiProviderDO extends OneapiBaseObject {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    Date gmtCreate;
    Date gmtModified;
    /**
     * 提供者代码，唯一标识
     */
    String code;
    /**
     * 服务商名称
     */
    String name;
    /**
     * 服务提供商主页
     */
    String url;
    /**
     * 基础api地址
     */
    String api;
    /**
     * 支持的模型
     */
    String models;
    /**
     * 是否启用
     */
    Boolean enable;
    /**
     * 该服务提供商的相关服务
     * 如: 获取账单、余额等
     */
    String service;
}
