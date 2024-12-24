package com.supersoft.oneapi.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface OneapiAccountMapper extends BaseMapper<OneapiAccountDO> {

    @Select("select gmt_modified from oneapi_account order by gmt_modified desc limit 1")
    Date lastModifiedTime();
}
