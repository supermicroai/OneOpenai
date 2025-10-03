package com.supersoft.oneapi.token.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supersoft.oneapi.token.data.OneapiTokenUsageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 令牌使用记录Mapper接口
 */
@Mapper
public interface OneapiTokenUsageMapper extends BaseMapper<OneapiTokenUsageDO> {
    
    /**
     * 查询使用记录
     * @param limit 限制条数
     * @return 使用记录列表
     */
    @Select("SELECT * FROM oneapi_token_usage ORDER BY gmt_create DESC LIMIT #{limit}")
    List<OneapiTokenUsageDO> selectAll(@Param("limit") Integer limit);
    
    /**
     * 根据时间范围查询使用记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表
     */
    @Select("SELECT * FROM oneapi_token_usage WHERE gmt_create >= #{startTime} AND gmt_create <= #{endTime} ORDER BY gmt_create DESC")
    List<OneapiTokenUsageDO> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 根据服务提供商查询使用记录
     * @param provider 服务提供商
     * @return 使用记录列表
     */
    @Select("SELECT * FROM oneapi_token_usage WHERE provider = #{provider} ORDER BY gmt_create DESC")
    List<OneapiTokenUsageDO> selectByProvider(@Param("provider") String provider);
    
    /**
     * 根据状态查询使用记录
     * @param status 状态
     * @return 使用记录列表
     */
    @Select("SELECT * FROM oneapi_token_usage WHERE status = #{status} ORDER BY gmt_create DESC")
    List<OneapiTokenUsageDO> selectByStatus(@Param("status") Integer status);
    
    /**
     * 按tokenId和模型分组统计使用记录
     * @param tokenId 令牌ID
     * @return 分组统计结果列表
     */
    @Select("SELECT token_id, provider, model, " +
            "SUM(request_tokens) as request_tokens, " +
            "SUM(response_tokens) as response_tokens " +
            "FROM oneapi_token_usage " +
            "WHERE token_id = #{tokenId} " +
            "GROUP BY token_id, provider, model")
    List<OneapiTokenUsageDO> selectGroupedUsageByTokenId(@Param("tokenId") Integer tokenId);
}
