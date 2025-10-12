package com.supersoft.oneapi.provider.service;

import com.supersoft.oneapi.provider.data.OneapiAccountDO;

public interface OneapiAccountService {
    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";

    /**
     * 获取已经花费和余额
     * @param account
     * @return
     */
    Double getCredits(OneapiAccountDO account);

    void analysis(String content);
}
