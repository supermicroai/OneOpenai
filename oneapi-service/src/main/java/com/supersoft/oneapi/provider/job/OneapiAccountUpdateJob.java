package com.supersoft.oneapi.provider.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiAccountMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiConfigUtils;
import com.supersoft.oneapi.util.OneapiDingTalkUtils;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OneapiAccountUpdateJob {
    public static final int MIN_CREDIT = 5;
    public static final String CREDIT_KEY = "oneapi.credit.min";
    @Resource
    OneapiProviderMapper oneapiProviderMapper;
    @Resource
    OneapiAccountMapper oneapiAccountMapper;

    @PostConstruct
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            QueryWrapper<OneapiProviderDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enable", true);
            List<OneapiProviderDO> list = oneapiProviderMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            QueryWrapper<OneapiAccountDO> queryAccount = new QueryWrapper<>();
            queryAccount.eq("status", 1);
            List<OneapiAccountDO> accounts = oneapiAccountMapper.selectList(queryAccount);
            if (CollectionUtils.isEmpty(accounts)) {
                return;
            }
            if (CollectionUtils.isEmpty(accounts)) {
                return;
            }
            Map<String, List<OneapiAccountDO>> accountMap = accounts.stream()
                    .collect(Collectors.groupingBy(OneapiAccountDO::getName));
            // 直接双重循环
            list.forEach(provider -> {
                String providerName = provider.getName();
                log.debug("开始更新账号余额: {}", providerName);
                List<OneapiAccountDO> providerAccounts = accountMap.get(providerName);
                if (CollectionUtils.isEmpty(providerAccounts)) {
                    return;
                }
                providerAccounts.forEach(account -> {
                    try {
                        String accountName = account.getName();
                        if (Objects.equals(providerName, accountName)) {
                            String accountService = provider.getService();
                            OneapiAccountService service = OneapiServiceLocator.getBeanSafe(accountService,
                                    OneapiAccountService.class);
                            if (service == null) {
                                return;
                            }
                            String apiKey = account.getApiKey();
                            if (StringUtils.isBlank(apiKey)) {
                                return;
                            }
                            boolean changed = service.getCredits(apiKey, account);
                            if (changed) {
                                double balance = account.getBalance();
                                // 如果余额低于阈值则发送告警
                                Integer limit = OneapiConfigUtils.getConfigWithDef(CREDIT_KEY, MIN_CREDIT);
                                if (balance < limit) {
                                    OneapiDingTalkUtils.sendAlert(String.format("账户余额不足%d元, 服务提供者:%s, 账号:%s",
                                            limit, provider.getName(), account.getNote()));
                                }
                                oneapiAccountMapper.updateById(account);
                            }
                        }
                    } catch (Exception e) {
                        log.error("获取余额失败", e);
                    }
                });
                log.debug("结束更新账号余额: {}", providerName);
            });
        }, 0, 10, TimeUnit.MINUTES);
    }
}
