package com.supersoft.oneapi.provider.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("oneapi_model")
public class OneapiModelDO extends OneapiBaseObject {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    String gmtCreate;
    String gmtModified;
    String name;
    String type;
    Boolean enable;
}
