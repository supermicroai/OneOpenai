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
@TableName("oneapi_account")
public class OneapiAccountDO extends OneapiBaseObject {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    Date gmtCreate;
    Date gmtModified;

    String providerCode;  // 关联的提供商代码
    String name;
    String note;

    String apiKey;
    // 部分账户需要直接使用ak sk来操作
    String ak;
    String sk;

    Double cost;
    Double balance;
    Boolean status;
}
