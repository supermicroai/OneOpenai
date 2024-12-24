package com.supersoft.oneapi.provider.service.account;

import com.alibaba.fastjson.JSON;
import com.supersoft.oneapi.common.exception.OneapiOutOfCreditException;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import com.supersoft.oneapi.util.OneapiHttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * openrouter仅能获取到使用, 无法获取到余额
 */
@Slf4j
@Service("openRouterService")
public class OneapiOpenRouterService implements OneapiAccountService {
    private static final String ERROR = "max_tokens limit exceeded";
    private static final String API = "https://openrouter.ai/api/v1/auth/key";

    @Override
    public boolean getCredits(String apiKey, OneapiAccountDO account) {
        String result = OneapiHttpUtils.get(API, Map.of(AUTHORIZATION, BEARER + apiKey));
        if (StringUtils.isBlank(result)) {
            return false;
        }
        OpenRouterResponse response = JSON.parseObject(result, OpenRouterResponse.class);
        if (response == null) {
            return false;
        }
        OpenRouterLimit data = response.getData();
        if (data == null) {
            return false;
        }
        account.setCost(OneapiCommonUtils.shortDouble(data.getUsage()));
        return true;
    }

    @Override
    public void analysis(String result) {
        if (result.contains(ERROR)) {
            throw new OneapiOutOfCreditException("账户余额不足");
        }
    }

    @Data
    public static class OpenRouterResponse {
        OpenRouterLimit data;
    }

    @Data
    public static class OpenRouterLimit {
        double usage;
    }
}
