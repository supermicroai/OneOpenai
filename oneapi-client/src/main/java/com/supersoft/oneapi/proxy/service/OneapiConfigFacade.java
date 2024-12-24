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
}
