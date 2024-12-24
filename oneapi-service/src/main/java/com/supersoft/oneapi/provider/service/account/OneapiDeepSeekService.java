package com.supersoft.oneapi.provider.service.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiHttpUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("deepSeekService")
public class OneapiDeepSeekService implements OneapiAccountService {
    private static final String API = "https://api.deepseek.com/user/balance";


    @Override
    public boolean getCredits(String apiKey, OneapiAccountDO account) {
        String result = OneapiHttpUtils.get(API, Map.of(AUTHORIZATION, BEARER + apiKey));
        if (StringUtils.isBlank(result)) {
            return false;
        }
        DeepSeekBalance balance = JSON.parseObject(result, DeepSeekBalance.class);
        if (balance == null) {
            return false;
        }
        List<DeepSeekBalanceInfo> infos = balance.getInfos();
        if (CollectionUtils.isEmpty(infos)) {
            return false;
        }
        DeepSeekBalanceInfo info = infos.getFirst();
        if (info == null) {
            return false;
        }
        account.setBalance(info.getBalance());
        return true;
    }

    @Override
    public void analysis(String errorMsg) {

    }

    @Data
    public static class DeepSeekBalance {
        @JSONField(name = "is_available")
        boolean available;
        @JSONField(name = "balance_infos")
        List<DeepSeekBalanceInfo> infos;
    }

    @Data
    public static class DeepSeekBalanceInfo {
        String currency;
        @JSONField(name = "topped_up_balance")
        double balance;
    }
}
