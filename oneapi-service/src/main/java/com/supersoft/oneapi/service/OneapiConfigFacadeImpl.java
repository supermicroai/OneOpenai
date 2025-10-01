package com.supersoft.oneapi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supersoft.oneapi.common.OneapiMultiResult;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiModelDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiAccountMapper;
import com.supersoft.oneapi.provider.mapper.OneapiModelMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.proxy.service.OneapiConfigFacade;
import com.supersoft.oneapi.token.data.OneapiTokenUsageDO;
import com.supersoft.oneapi.token.service.OneapiTokenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OneapiConfigFacadeImpl implements OneapiConfigFacade {
    @Resource
    OneapiModelMapper modelMapper;
    @Resource
    OneapiProviderMapper providerMapper;
    @Resource
    OneapiAccountMapper accountMapper;
    @Resource
    OneapiTokenService oneapiTokenService;

    @Override
    public OneapiMultiResult<OneapiModelDO> getModels() {
        QueryWrapper<OneapiModelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enable", true);
        List<OneapiModelDO> models = modelMapper.selectList(queryWrapper);
        return OneapiMultiResult.success(models);
    }

    @Override
    public OneapiMultiResult<OneapiProviderDO> getProviders() {
        QueryWrapper<OneapiProviderDO> queryWrapper = new QueryWrapper<>();
        List<OneapiProviderDO> list = providerMapper.selectList(queryWrapper);
        return OneapiMultiResult.success(list);
    }

    @Override
    public OneapiSingleResult<OneapiProviderDO> getProvider(Long id) {
        OneapiProviderDO provider = providerMapper.selectById(id);
        return OneapiSingleResult.success(provider);
    }

    @Override
    public OneapiSingleResult<Boolean> enableProvider(Long id, Boolean enable) {
        OneapiProviderDO provider = providerMapper.selectById(id);
        if (provider == null) {
            return OneapiSingleResult.fail("Provider not found");
        }
        provider.setGmtModified(new Date());
        provider.setEnable(enable);
        providerMapper.updateById(provider);
        return OneapiSingleResult.success();
    }

    @Override
    public OneapiMultiResult<OneapiAccountDO> getAccounts(Long id) {
        OneapiProviderDO selected = providerMapper.selectById(id);
        if (selected == null) {
            return OneapiMultiResult.fail("Provider not found");
        }
        
        QueryWrapper<OneapiAccountDO> queryAccount = new QueryWrapper<>();
        queryAccount.eq("provider_code", selected.getCode());
        List<OneapiAccountDO> accounts = accountMapper.selectList(queryAccount);
        OneapiMultiResult<OneapiAccountDO> result = OneapiMultiResult.success(accounts);
        result.addParam("providerId", id);
        result.addParam("providerCode", selected.getCode());
        result.addParam("name", selected.getName());
        return result;
    }

    @Override
    public OneapiSingleResult<OneapiProviderDO> updateProvider(OneapiProviderDO provider) {
        Long id = provider.getId();
        if (id == null) {
            return OneapiSingleResult.fail("Provider id is null");
        }
        provider.setGmtModified(new Date());
        providerMapper.updateById(provider);
        return OneapiSingleResult.success(provider);
    }

    @Override
    public OneapiSingleResult<OneapiAccountDO> updateAccount(OneapiAccountDO account) {
        Long id = account.getId();
        if (id == null) {
            // 默认给一个初始余额
            account.setBalance(5d);
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
        return OneapiSingleResult.success(account);
    }

    @Override
    public OneapiSingleResult<Boolean> enableAccount(Long id, Boolean enable) {
        OneapiAccountDO account = accountMapper.selectById(id);
        if (account == null) {
            return OneapiSingleResult.fail("Account not found");
        }
        account.setGmtModified(new Date());
        account.setStatus(enable);
        accountMapper.updateById(account);
        return OneapiSingleResult.success();
    }

    @Override
    public OneapiSingleResult<Boolean> deleteAccount(Long id) {
        OneapiAccountDO account = accountMapper.selectById(id);
        if (account == null) {
            return OneapiSingleResult.fail("Account not found");
        }
        accountMapper.deleteById(id);
        return OneapiSingleResult.success();
    }

    @Override
    public OneapiMultiResult<OneapiTokenUsageDO> queryTokenUsageRecords(
            String provider, String model, Integer status, String startTime, String endTime, Integer page, Integer pageSize) {
        return oneapiTokenService.queryUsageRecords(provider, model, status, startTime, endTime, page, pageSize);
    }
}
