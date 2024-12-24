package com.supersoft.oneapi.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface OneapiProviderMapper extends BaseMapper<OneapiProviderDO> {

    @Select("select gmt_modified from oneapi_provider order by gmt_modified desc limit 1")
    Date lastModifiedTime();
}
