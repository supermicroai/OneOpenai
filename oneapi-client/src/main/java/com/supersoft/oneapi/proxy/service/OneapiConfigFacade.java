package com.supersoft.oneapi.proxy.service;

import com.supersoft.oneapi.common.OneapiMultiResult;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiModelDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;

public interface OneapiConfigFacade {
    /**
     * 获取支持的模型列表名称
     * @return
     */
    OneapiMultiResult<OneapiModelDO> getModels();

    /**
     * 获取支持的提供商列表
     * @return
     */
    OneapiMultiResult<OneapiProviderDO> getProviders();

    /**
     * 获取指定提供商的信息
     * @param id
     * @return
     */
    OneapiSingleResult<OneapiProviderDO> getProvider(Long id);

    /**
     * 启用或禁用指定提供商
     * @param id
     * @param enable
     * @return
     */
    OneapiSingleResult<Boolean> enableProvider(Long id, Boolean enable);

    /**
     * 获取指定提供商的账号信息
     * @param id
     * @return
     */
    OneapiMultiResult<OneapiAccountDO> getAccounts(Long id);

    /**
     * 更新提供商信息
     * @param provider
     * @return
     */
    OneapiSingleResult<OneapiProviderDO> updateProvider(OneapiProviderDO provider);

    /**
     * 更新账号信息
     * @param account
     * @return
     */
    OneapiSingleResult<OneapiAccountDO> updateAccount(OneapiAccountDO account);

    /**
     * 启用或禁用账号
     * @param id
     * @param enable
     * @return
     */
    OneapiSingleResult<Boolean> enableAccount(Long id, Boolean enable);

    /**
     * 删除账号
     * @param id
     * @return
     */
    OneapiSingleResult<Boolean> deleteAccount(Long id);

    /**
     * 查询访问日志（令牌使用记录）
     * @param provider 服务提供商
     * @param model 模型
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param pageSize 页大小
     * @return 使用记录列表
     */
    OneapiMultiResult<com.supersoft.oneapi.token.data.OneapiTokenUsageDO> queryTokenUsageRecords(
        String provider, String model, Integer status, String startTime, String endTime, Integer page, Integer pageSize
    );
}
