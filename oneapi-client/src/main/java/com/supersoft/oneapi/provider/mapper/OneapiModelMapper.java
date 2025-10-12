package com.supersoft.oneapi.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.provider.data.OneapiModelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface OneapiModelMapper extends BaseMapper<OneapiModelDO> {

    @Select("select name from oneapi_model where enable = 1")
    Set<String> selectModelNames();
    
    /**
     * 根据名称查询模型
     * @param name 模型名称
     * @return 模型信息
     */
    @Select("SELECT * FROM oneapi_model WHERE name = #{name} LIMIT 1")
    OneapiModelDO selectByName(@Param("name") String name);
    
    /**
     * 根据提供商和模型名称查询模型
     * @param vendor 提供商名称
     * @param name 模型名称
     * @return 模型信息
     */
    @Select("SELECT * FROM oneapi_model WHERE vendor = #{vendor} AND name = #{name} LIMIT 1")
    OneapiModelDO selectByVendorAndName(@Param("vendor") String vendor, @Param("name") String name);
}