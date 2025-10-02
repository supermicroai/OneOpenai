
package com.supersoft.oneapi.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface OneapiProviderMapper extends BaseMapper<OneapiProviderDO> {

    @Select("select gmt_modified from oneapi_provider order by gmt_modified desc limit 1")
    Date lastModifiedTime();
    
    /**
     * 根据名称查询供应商
     * @param name 供应商名称
     * @return 供应商信息
     */
    @Select("SELECT * FROM oneapi_provider WHERE name = #{name} LIMIT 1")
    OneapiProviderDO selectByName(@Param("name") String name);
}
