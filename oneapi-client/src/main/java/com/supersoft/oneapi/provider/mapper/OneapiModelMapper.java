package com.supersoft.oneapi.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.provider.data.OneapiModelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface OneapiModelMapper extends BaseMapper<OneapiModelDO> {

    @Select("select name from oneapi_model where enable = 1")
    Set<String> selectModelNames();
}
