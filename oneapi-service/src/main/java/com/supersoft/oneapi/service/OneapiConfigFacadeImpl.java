package com.supersoft.oneapi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.supersoft.oneapi.common.OneapiMultiResult;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.config.data.OneapiConfigDO;
import com.supersoft.oneapi.config.mapper.OneapiConfigMapper;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiModelDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiAccountMapper;
import com.supersoft.oneapi.provider.mapper.OneapiModelMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.provider.job.OneapiAccountUpdateJob;
import com.supersoft.oneapi.proxy.service.OneapiConfigFacade;
import com.supersoft.oneapi.service.alert.OneapiAlertManager;
import com.supersoft.oneapi.token.data.OneapiTokenUsageDO;
import com.supersoft.oneapi.token.service.OneapiTokenService;
import com.supersoft.oneapi.util.BooleanUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
    @Resource
    OneapiConfigMapper configMapper;
    @Resource
    OneapiAlertManager oneapiAlertManager;
    @Resource
    OneapiAccountUpdateJob oneapiAccountUpdateJob;

    @Override
    public OneapiMultiResult<OneapiModelDO> getModels() {
        QueryWrapper<OneapiModelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("type", "vendor", "name"); // 按类型、厂商、名称排序
        List<OneapiModelDO> models = modelMapper.selectList(queryWrapper);
        return OneapiMultiResult.success(models);
    }
    
    @Override
    public OneapiSingleResult<OneapiModelDO> addModel(OneapiModelDO model) {
        try {
            Date now = new Date();
            model.setGmtCreate(now);
            model.setGmtModified(now);
            modelMapper.insert(model);
            return OneapiSingleResult.success(model);
        } catch (Exception e) {
            log.error("添加模型异常", e);
            return OneapiSingleResult.fail("添加模型失败: " + e.getMessage());
        }
    }
    
    @Override
    public OneapiSingleResult<OneapiModelDO> updateModel(OneapiModelDO model) {
        try {
            model.setGmtModified(new Date());
            modelMapper.updateById(model);
            return OneapiSingleResult.success(model);
        } catch (Exception e) {
            log.error("更新模型异常", e);
            return OneapiSingleResult.fail("更新模型失败: " + e.getMessage());
        }
    }
    
    @Override
    public OneapiSingleResult<Boolean> deleteModel(Long modelId) {
        try {
            OneapiModelDO model = modelMapper.selectById(modelId);
            if (model == null) {
                return OneapiSingleResult.fail("模型不存在");
            }
            modelMapper.deleteById(modelId);
            return OneapiSingleResult.success(true);
        } catch (Exception e) {
            log.error("删除模型异常", e);
            return OneapiSingleResult.fail("删除模型失败: " + e.getMessage());
        }
    }
    
    @Override
    public OneapiSingleResult<Boolean> toggleModel(Long modelId, Boolean enabled) {
        try {
            OneapiModelDO model = modelMapper.selectById(modelId);
            if (model == null) {
                return OneapiSingleResult.fail("模型不存在");
            }
            model.setEnable(enabled);
            model.setGmtModified(new Date());
            modelMapper.updateById(model);
            return OneapiSingleResult.success(true);
        } catch (Exception e) {
            log.error("切换模型状态异常", e);
            return OneapiSingleResult.fail("切换模型状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public OneapiMultiResult<OneapiModelDO> getEnabledModels(String type) {
        try {
            QueryWrapper<OneapiModelDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enable", BooleanUtils.toInteger(true));
            if (type != null && !type.trim().isEmpty()) {
                queryWrapper.eq("type", type);
            }
            List<OneapiModelDO> models = modelMapper.selectList(queryWrapper);
            return OneapiMultiResult.success(models);
        } catch (Exception e) {
            log.error("获取已启用模型异常", e);
            return OneapiMultiResult.fail("获取已启用模型失败: " + e.getMessage());
        }
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
        try {
            Long id = provider.getId();
            Date now = new Date();
            
            if (id == null) {
                // 新增提供商
                if (StringUtils.isNotEmpty(provider.getCode())) {
                    return OneapiSingleResult.fail("提供商代码不能为空");
                }
                provider.setGmtCreate(now);
                provider.setGmtModified(now);
                if (provider.getEnable() == null) {
                    provider.setEnable(true); // 默认启用
                }
                providerMapper.insert(provider);
            } else {
                // 更新提供商
                provider.setGmtModified(now);
                providerMapper.updateById(provider);
            }
            
            return OneapiSingleResult.success(provider);
        } catch (Exception e) {
            log.error("保存提供商信息异常", e);
            // 判断是否为唯一性约束异常
            if (e instanceof DuplicateKeyException) {
                return OneapiSingleResult.fail("提供商代码已存在，请使用其他代码");
            }
            return OneapiSingleResult.fail("保存提供商信息失败：" + e.getMessage());
        }
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
        account.setStatus(BooleanUtils.toInteger(enable));
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

    @Override
    public OneapiMultiResult<OneapiConfigDO> getConfigs() {
        try {
            List<OneapiConfigDO> configs = configMapper.selectList(null);
            return OneapiMultiResult.success(configs);
        } catch (Exception e) {
            log.error("获取系统配置异常", e);
            return OneapiMultiResult.fail("获取系统配置失败: " + e.getMessage());
        }
    }

    @Override
    public OneapiSingleResult<OneapiConfigDO> updateConfig(OneapiConfigDO config) {
        try {
            if (config.getId() == null) {
                return OneapiSingleResult.fail("配置ID不能为空");
            }
            config.setGmtModified(new Date());
            configMapper.updateById(config);
            return OneapiSingleResult.success(config);
        } catch (Exception e) {
            log.error("更新系统配置异常", e);
            return OneapiSingleResult.fail("更新系统配置失败: " + e.getMessage());
        }
    }

    @Override
    public OneapiSingleResult<Boolean> updateAllAccountBalances() {
        try {
            log.info("手动触发更新所有账户余额");
            OneapiAccountUpdateJob.UpdateResult result = oneapiAccountUpdateJob.updateAllAccountBalances();
            
            if (result.getFailCount() > 0) {
                log.warn("更新账户余额完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
                return OneapiSingleResult.success(true);
            } else {
                log.info("更新账户余额完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
                return OneapiSingleResult.success(true);
            }
            
        } catch (Exception e) {
            log.error("手动更新账户余额异常", e);
            return OneapiSingleResult.fail("更新账户余额失败: " + e.getMessage());
        }
    }
}
