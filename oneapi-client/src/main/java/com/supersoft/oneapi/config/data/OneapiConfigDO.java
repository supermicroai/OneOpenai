package com.supersoft.oneapi.config.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("oneapi_config")
public class OneapiConfigDO extends OneapiBaseObject {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    String gmtCreate;
    String gmtModified;

    String configGroup;

    String configKey;
    String configValue;

    String note;
    Integer status;
}
