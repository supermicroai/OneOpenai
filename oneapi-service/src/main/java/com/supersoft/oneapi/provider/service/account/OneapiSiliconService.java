package com.supersoft.oneapi.provider.service.account;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import com.supersoft.oneapi.util.OneapiHttpUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("siliconService")
public class OneapiSiliconService implements OneapiAccountService {
    private static final String API = "https://api.siliconflow.cn/v1/user/info";

    @Override
    public Double getCredits(OneapiAccountDO account) {
        String apiKey = account.getApiKey();
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        
        String result = OneapiHttpUtils.get(API, Map.of(AUTHORIZATION, BEARER + apiKey));
        if (StringUtils.isBlank(result)) {
            return null;
        }
        SiliconResponse response = JSON.parseObject(result, SiliconResponse.class);
        if (response == null) {
            return null;
        }
        SiliconUserInfo data = response.getData();
        if (data == null) {
            return null;
        }
        double balance = OneapiCommonUtils.shortDouble(data.getTotalBalance());
        account.setBalance(balance);
        return balance;
    }

    @Override
    public void analysis(String errorMsg) {

    }

    @Data
    public static class SiliconResponse {
        String message;
        String code;
        Boolean status;
        SiliconUserInfo data;
    }

    @Data
    public static class SiliconUserInfo {
        String id;
        String name;
        String status;
        /**
         * 赠送金额
         */
        double balance;
        /**
         * 充值金额
         */
        double chargeBalance;
        /**
         * 总金额
         */
        double totalBalance;
    }
}