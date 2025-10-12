package com.supersoft.oneapi.provider.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiAccountMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.provider.service.OneapiAccountService;
import com.supersoft.oneapi.util.OneapiConfigUtils;
import com.supersoft.oneapi.service.alert.OneapiAlertManager;
import com.supersoft.oneapi.util.OneapiServiceLocator;
import jakarta.annotation.Resource;
import com.supersoft.oneapi.util.BooleanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OneapiAccountUpdateJob {
    public static final int MIN_CREDIT = 5;
    public static final String CREDIT_KEY = "oneapi.credit.min";
    @Resource
    OneapiAlertManager oneapiAlertManager;
    @Resource
    OneapiAccountMapper oneapiAccountMapper;
    @Resource
    OneapiProviderMapper oneapiProviderMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("应用启动完成，开始调度账户余额更新任务");
        Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(this::updateAllAccountBalances, 1, 10, TimeUnit.MINUTES);
    }

    /**
     * 更新所有账户余额
     * @return 更新结果统计
     */
    public UpdateResult updateAllAccountBalances() {
        try {
            log.info("开始更新所有账户余额");
            
            // 获取所有启用的提供商
            QueryWrapper<OneapiProviderDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enable", BooleanUtils.toInteger(true));
            List<OneapiProviderDO> providers = oneapiProviderMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(providers)) {
                log.info("没有找到启用的提供商");
                return new UpdateResult(0, 0);
            }
            
            // 获取所有启用的账户
            QueryWrapper<OneapiAccountDO> queryAccount = new QueryWrapper<>();
            queryAccount.eq("status", BooleanUtils.toInteger(true));
            List<OneapiAccountDO> accounts = oneapiAccountMapper.selectList(queryAccount);
            if (CollectionUtils.isEmpty(accounts)) {
                log.info("没有找到启用的账户");
                return new UpdateResult(0, 0);
            }
            
            // 按提供商代码分组账户
            Map<String, List<OneapiAccountDO>> accountMap = accounts.stream()
                    .collect(Collectors.groupingBy(OneapiAccountDO::getProviderCode));
            
            int successCount = 0;
            int failCount = 0;
            
            // 遍历所有提供商
            for (OneapiProviderDO provider : providers) {
                String providerCode = provider.getCode();
                log.debug("开始更新提供商 {} 的账号余额", providerCode);
                
                List<OneapiAccountDO> providerAccounts = accountMap.get(providerCode);
                if (CollectionUtils.isEmpty(providerAccounts)) {
                    log.debug("提供商 {} 没有关联的账户", providerCode);
                    continue;
                }
                
                // 遍历该提供商的所有账户
                for (OneapiAccountDO account : providerAccounts) {
                    try {
                        String accountService = provider.getService();
                        OneapiAccountService service = OneapiServiceLocator.getBeanSafe(accountService,
                                OneapiAccountService.class);
                        if (service == null) {
                            log.warn("找不到服务实现: {}", accountService);
                            failCount++;
                            continue;
                        }
                        
                        // 获取余额
                        Double balance = service.getCredits(account);
                        if (balance != null) {
                            // 更新账户余额
                            account.setBalance(balance);
                            account.setGmtModified(new Date());
                            oneapiAccountMapper.updateById(account);
                            successCount++;
                            
                            // 检查余额是否低于阈值
                            Integer limit = OneapiConfigUtils.getConfigWithDef(CREDIT_KEY, MIN_CREDIT);
                            if (limit != null && balance < limit) {
                                oneapiAlertManager.sendAlert(String.format("账户余额不足%d元, 服务提供者:%s, 账号:%s",
                                        limit, provider.getName(), account.getNote()));
                            }
                            
                            log.debug("成功更新账户 {} 余额为 {}", account.getNote(), balance);
                        } else {
                            log.warn("获取账户 {} 余额失败", account.getNote());
                            failCount++;
                        }
                    } catch (Exception e) {
                        log.error("更新账户 {} 余额异常", account.getNote(), e);
                        failCount++;
                    }
                }
                
                log.debug("完成更新提供商 {} 的账号余额", providerCode);
            }
            
            log.info("更新账户余额完成，成功: {}, 失败: {}", successCount, failCount);
            return new UpdateResult(successCount, failCount);
            
        } catch (Exception e) {
            log.error("更新账户余额异常", e);
            return new UpdateResult(0, 1);
        }
    }

    /**
     * 更新结果统计类
     */
    public static class UpdateResult {
        private final int successCount;
        private final int failCount;
        
        public UpdateResult(int successCount, int failCount) {
            this.successCount = successCount;
            this.failCount = failCount;
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getFailCount() {
            return failCount;
        }
        
        public int getTotalCount() {
            return successCount + failCount;
        }
    }
}