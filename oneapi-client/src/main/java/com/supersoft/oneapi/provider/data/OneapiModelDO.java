package com.supersoft.oneapi.provider.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supersoft.oneapi.common.OneapiBaseObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("oneapi_model")
public class OneapiModelDO extends OneapiBaseObject {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    Date gmtCreate;
    Date gmtModified;
    String name;
    String vendor;
    String type;
    Boolean enable;
    /**
     * 输入token价格（每1M个token）
     */
    BigDecimal inputPrice;
    /**
     * 输出token价格（每1M个token）
     */
    BigDecimal outputPrice;
    /**
     * 模型描述
     */
    String description;
}
