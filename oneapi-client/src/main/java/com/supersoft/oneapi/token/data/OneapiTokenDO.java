package com.supersoft.oneapi.token.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 令牌数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oneapi_token")
public class OneapiTokenDO extends OneapiBaseObject {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 令牌名称
     */
    private String name;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * 令牌描述
     */
    private String description;
    
    /**
     * 过期时间，null表示永不过期
     */
    private LocalDateTime expireTime;
    
    /**
     * 最大token数限制，-1表示不限制
     */
    private Long maxUsage;
    
    /**
     * 当前token使用量
     */
    private Long tokenUsage;
    
    /**
     * 状态：1启用，0禁用
     */
    private Integer status;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;
}
