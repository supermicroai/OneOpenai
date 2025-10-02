package com.supersoft.oneapi.token.service;

import com.supersoft.oneapi.common.OneapiMultiResult;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.token.data.OneapiTokenDO;
import com.supersoft.oneapi.token.data.OneapiTokenUsageDO;

import java.util.Date;
import java.util.List;

/**
 * 令牌服务接口
 */
public interface OneapiTokenService {
    
    /**
     * 创建令牌
     * @param name 令牌名称
     * @param description 描述
     * @param expireTime 过期时间
     * @param maxUsage 最大使用次数
     * @param creator 创建者
     * @return 创建结果
     */
    OneapiSingleResult<OneapiTokenDO> createToken(String name, String description, 
                                                  Date expireTime, Long maxUsage, String creator);
    
    /**
     * 删除令牌
     * @param id 令牌ID
     * @return 删除结果
     */
    OneapiSingleResult<Boolean> deleteToken(Integer id);
    
    /**
     * 更新令牌
     * @param token 令牌对象
     * @return 更新结果
     */
    OneapiSingleResult<Boolean> updateToken(OneapiTokenDO token);
    
    /**
     * 查询所有令牌
     * @return 令牌列表
     */
    OneapiMultiResult<OneapiTokenDO> getAllTokens();
    
    /**
     * 根据ID查询令牌
     * @param id 令牌ID
     * @return 令牌对象
     */
    OneapiSingleResult<OneapiTokenDO> getTokenById(Integer id);
    
    /**
     * 验证API密钥
     * @param apiKey API密钥
     * @return 验证结果，包含令牌信息
     */
    OneapiSingleResult<OneapiTokenDO> validateApiKey(String apiKey);
    
    /**
     * 记录令牌使用
     * @param provider 服务提供商
     * @param model 模型
     * @param requestTokens 请求令牌数
     * @param responseTokens 响应令牌数
     * @param status 状态
     * @param errorMsg 错误信息
     * @param ipAddress IP地址
     * @return 记录结果
     */
    OneapiSingleResult<Boolean> recordUsage(String provider, String model, 
                                           Integer requestTokens, Integer responseTokens, 
                                           Integer status, String errorMsg, 
                                           String ipAddress);
    
    /**
     * 获取使用记录
     * @param limit 限制数量
     * @return 使用记录列表
     */
    OneapiMultiResult<OneapiTokenUsageDO> getUsageRecords(Integer limit);
    
    /**
     * 查询使用记录（带筛选条件）
     * @param provider 服务提供商
     * @param model 模型
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param pageSize 页大小
     * @return 使用记录列表和总数
     */
    OneapiMultiResult<OneapiTokenUsageDO> queryUsageRecords(String provider, String model, 
                                                           Integer status, String startTime, 
                                                           String endTime, Integer page, 
                                                           Integer pageSize);
    
    /**
     * 生成新的API密钥
     * @return API密钥
     */
    String generateApiKey();
}
