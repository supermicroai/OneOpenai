package com.supersoft.oneapi.token.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.token.data.OneapiTokenDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 令牌Mapper接口
 */
@Mapper
public interface OneapiTokenMapper extends BaseMapper<OneapiTokenDO> {
    
    /**
     * 根据API密钥查询令牌
     * @param apiKey API密钥
     * @return 令牌对象
     */
    @Select("SELECT * FROM oneapi_token WHERE api_key = #{apiKey}")
    OneapiTokenDO selectByApiKey(@Param("apiKey") String apiKey);
    
    /**
     * 根据状态查询令牌
     * @param status 状态
     * @return 令牌列表
     */
    @Select("SELECT * FROM oneapi_token WHERE status = #{status} ORDER BY gmt_create DESC")
    List<OneapiTokenDO> selectByStatus(@Param("status") Integer status);
    
    /**
     * 查询所有令牌
     * @return 令牌列表
     */
    @Select("SELECT * FROM oneapi_token ORDER BY gmt_create DESC")
    List<OneapiTokenDO> selectAll();
    
    /**
     * 更新令牌使用次数
     * @param id 令牌ID
     * @param increment 增量
     * @return 影响行数
     */
    @Update("UPDATE oneapi_token SET current_usage = current_usage + #{increment}, last_used_time = CURRENT_TIMESTAMP, gmt_modified = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateUsageCount(@Param("id") Integer id, @Param("increment") Long increment);
    
    /**
     * 更新最后使用时间
     * @param id 令牌ID
     * @return 影响行数
     */
    @Update("UPDATE oneapi_token SET last_used_time = CURRENT_TIMESTAMP, gmt_modified = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateLastUsedTime(@Param("id") Integer id);
}
