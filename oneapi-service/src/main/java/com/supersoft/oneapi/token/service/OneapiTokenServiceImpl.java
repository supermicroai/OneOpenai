package com.supersoft.oneapi.token.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supersoft.oneapi.common.OneapiMultiResult;
import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.service.OneapiPricingService;
import com.supersoft.oneapi.token.data.OneapiTokenDO;
import com.supersoft.oneapi.token.data.OneapiTokenUsageDO;
import com.supersoft.oneapi.token.mapper.OneapiTokenMapper;
import com.supersoft.oneapi.token.mapper.OneapiTokenUsageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 令牌服务实现类
 */
@Service("oneapiTokenService")
@Slf4j
public class OneapiTokenServiceImpl implements OneapiTokenService {
    
    @Resource
    private OneapiTokenMapper tokenMapper;
    
    @Resource
    private OneapiTokenUsageMapper usageMapper;
    
    @Resource
    private OneapiPricingService pricingService;
    
    @Override
    @Transactional
    public OneapiSingleResult<OneapiTokenDO> createToken(String name, String description, 
                                                         Date expireTime, Long maxUsage, BigDecimal maxCostLimit, String creator) {
        if (StringUtils.isBlank(name)) {
            return OneapiSingleResult.fail("令牌名称不能为空");
        }
        
        try {
            OneapiTokenDO token = new OneapiTokenDO();
            token.setName(name);
            token.setApiKey(generateApiKey());
            token.setDescription(description);
            token.setExpireTime(expireTime);
            token.setMaxUsage(maxUsage == null ? -1L : maxUsage);
            token.setTokenUsage(0L);
            token.setMaxCostLimit(maxCostLimit == null ? BigDecimal.valueOf(-1) : maxCostLimit);
            token.setCurrentCostUsage(BigDecimal.ZERO);
            token.setStatus(1);
            token.setCreator(creator);
            
            int result = tokenMapper.insert(token);
            if (result > 0) {
                return OneapiSingleResult.success(token);
            } else {
                return OneapiSingleResult.fail("创建令牌失败");
            }
        } catch (Exception e) {
            log.error("创建令牌异常", e);
            return OneapiSingleResult.fail("创建令牌异常：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public OneapiSingleResult<Boolean> deleteToken(Integer id) {
        if (id == null || id <= 0) {
            return OneapiSingleResult.fail("令牌ID无效");
        }
        
        try {
            int result = tokenMapper.deleteById(id);
            return OneapiSingleResult.success(result > 0);
        } catch (Exception e) {
            log.error("删除令牌异常", e);
            return OneapiSingleResult.fail("删除令牌异常：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public OneapiSingleResult<Boolean> updateToken(OneapiTokenDO token) {
        if (token == null || token.getId() == null) {
            return OneapiSingleResult.fail("令牌信息无效");
        }
        
        try {
            int result = tokenMapper.updateById(token);
            return OneapiSingleResult.success(result > 0);
        } catch (Exception e) {
            log.error("更新令牌异常", e);
            return OneapiSingleResult.fail("更新令牌异常：" + e.getMessage());
        }
    }
    
    @Override
    public OneapiMultiResult<OneapiTokenDO> getAllTokens() {
        try {
            List<OneapiTokenDO> tokens = tokenMapper.selectAll();
            return OneapiMultiResult.success(tokens);
        } catch (Exception e) {
            log.error("查询令牌列表异常", e);
            return OneapiMultiResult.fail("查询令牌列表异常：" + e.getMessage());
        }
    }
    
    @Override
    public OneapiSingleResult<OneapiTokenDO> getTokenById(Integer id) {
        if (id == null || id <= 0) {
            return OneapiSingleResult.fail("令牌ID无效");
        }
        
        try {
            OneapiTokenDO token = tokenMapper.selectById(id);
            return OneapiSingleResult.success(token);
        } catch (Exception e) {
            log.error("查询令牌异常", e);
            return OneapiSingleResult.fail("查询令牌异常：" + e.getMessage());
        }
    }
    
    @Override
    public OneapiSingleResult<OneapiTokenDO> validateApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return OneapiSingleResult.fail("API密钥不能为空");
        }
        
        try {
            OneapiTokenDO token = tokenMapper.selectByApiKey(apiKey);
            if (token == null) {
                return OneapiSingleResult.fail("API密钥无效");
            }
            
            // 检查令牌状态
            if (token.getStatus() != 1) {
                return OneapiSingleResult.fail("令牌已禁用");
            }
            
            // 检查过期时间
            if (token.getExpireTime() != null && token.getExpireTime().before(new Date())) {
                return OneapiSingleResult.fail("令牌已过期");
            }

            BigDecimal maxCostLimit = token.getMaxCostLimit();
            BigDecimal costUsage = token.getCurrentCostUsage();
            // 检查费用限制（优先级高于token限制）
            if (maxCostLimit != null && maxCostLimit.compareTo(BigDecimal.valueOf(-1)) != 0) {
                // 直接使用token表中的当前费用使用量字段
                BigDecimal currentCostUsage = costUsage != null ? costUsage : BigDecimal.ZERO;
                if (currentCostUsage.compareTo(maxCostLimit) >= 0) {
                    return OneapiSingleResult.fail("令牌费用已达上限");
                }
            } else {
                // 如果没有设置费用限制，检查token使用量限制
                if (token.getMaxUsage() != -1 && token.getTokenUsage() >= token.getMaxUsage()) {
                    return OneapiSingleResult.fail("令牌token使用量已达上限");
                }
            }
            
            return OneapiSingleResult.success(token);
        } catch (Exception e) {
            log.error("验证API密钥异常", e);
            return OneapiSingleResult.fail("验证API密钥异常：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public OneapiSingleResult<Boolean> recordUsage(Integer tokenId, String provider, String model, 
                                                   Integer requestTokens, Integer responseTokens, 
                                                   Integer status, String errorMsg, 
                                                   String ipAddress) {
        try {
            OneapiTokenUsageDO usage = new OneapiTokenUsageDO();
            usage.setTokenId(tokenId);
            usage.setProvider(provider);
            usage.setModel(model);
            usage.setRequestTokens(requestTokens == null ? 0 : requestTokens);
            usage.setResponseTokens(responseTokens == null ? 0 : responseTokens);
            
            // 使用价格计算服务计算成本
            BigDecimal cost = pricingService.calculateCost(provider, model, requestTokens, responseTokens);
            log.debug("价格计算结果: provider={}, model={}, requestTokens={}, responseTokens={}, cost={}", 
                provider, model, requestTokens, responseTokens, cost);
            usage.setCost(cost);
            
            usage.setStatus(status);
            usage.setErrorMsg(errorMsg);
            usage.setIpAddress(ipAddress);
            // 显式设置创建时间和修改时间
            Date now = new Date();
            usage.setGmtCreate(now);
            usage.setGmtModified(now);
            
            // 插入使用记录
            int result = usageMapper.insert(usage);
            
            // 同时更新令牌的token使用量和费用使用量
            if (result > 0 && tokenId != null) {
                Long totalTokens = (long) ((requestTokens == null ? 0 : requestTokens) + (responseTokens == null ? 0 : responseTokens));
                tokenMapper.updateUsageAndCost(tokenId, totalTokens, cost);
                log.debug("更新令牌使用量: tokenId={}, totalTokens={}, cost={}", tokenId, totalTokens, cost);
            }
            
            return OneapiSingleResult.success(result > 0);
        } catch (Exception e) {
            log.error("记录使用异常", e);
            return OneapiSingleResult.fail("记录使用异常：" + e.getMessage());
        }
    }
    
    @Override
    public OneapiMultiResult<OneapiTokenUsageDO> getUsageRecords(Integer limit) {
        try {
            List<OneapiTokenUsageDO> records = usageMapper.selectAll(limit);
            return OneapiMultiResult.success(records);
        } catch (Exception e) {
            log.error("查询使用记录异常", e);
            return OneapiMultiResult.fail("查询使用记录异常：" + e.getMessage());
        }
    }
    
    @Override
    public String generateApiKey() {
        return "sk-oneapi-" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Override
    @Transactional
    public OneapiSingleResult recalculateAllTokenCosts() {
        try {
            log.info("开始重新计算所有令牌的费用使用量");
            
            // 获取所有令牌
            List<OneapiTokenDO> allTokens = tokenMapper.selectList(null);
            if (allTokens == null || allTokens.isEmpty()) {
                return OneapiSingleResult.success("没有找到令牌，无需计算");
            }
            
            for (OneapiTokenDO token : allTokens) {
                try {
                    // 查询该令牌的所有使用记录，按模型名分组统计
                    List<OneapiTokenUsageDO> usageRecords = usageMapper.selectGroupedUsageByTokenId(token.getId());
                    
                    if (usageRecords == null || usageRecords.isEmpty()) {
                        // 如果没有使用记录，将费用使用量重置为0
                        token.setCurrentCostUsage(BigDecimal.ZERO);
                        tokenMapper.updateById(token);
                        continue;
                    }
                    
                    // 按模型计算成本并累加
                    BigDecimal totalCost = BigDecimal.ZERO;
                    for (OneapiTokenUsageDO groupedUsage : usageRecords) {
                        BigDecimal modelCost = pricingService.calculateCost(
                            groupedUsage.getProvider(),
                            groupedUsage.getModel(), 
                            groupedUsage.getRequestTokens(), 
                            groupedUsage.getResponseTokens()
                        );
                        totalCost = totalCost.add(modelCost);
                        log.info("令牌: {} (ID: {}), 模型: {}, 输入Token: {}, 输出Token: {}, 成本: {}",
                            token.getName(), token.getId(), groupedUsage.getModel(), 
                            groupedUsage.getRequestTokens(), groupedUsage.getResponseTokens(), modelCost);
                    }
                    
                    // 更新令牌的总费用使用量
                    token.setCurrentCostUsage(totalCost);
                    tokenMapper.updateById(token);
                    log.info("已处理令牌: {} (ID: {}), 总成本: {}, 模型数: {}",
                        token.getName(), token.getId(), totalCost, usageRecords.size());
                } catch (Exception e) {
                    log.error("处理令牌 {} 时出错", token.getId(), e);
                }
            }
            return OneapiSingleResult.success();
        } catch (Exception e) {
            log.error("重新计算令牌费用异常", e);
            return OneapiSingleResult.fail("重新计算令牌费用异常：" + e.getMessage());
        }
    }
    


    @Override
    public OneapiMultiResult<OneapiTokenUsageDO> queryUsageRecords(String provider, String model, Integer status, String startTime, String endTime, Integer page, Integer pageSize) {
        try {
            // 参数验证和默认值设置
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            
            // 构建查询条件（不包含排序）
            QueryWrapper<OneapiTokenUsageDO> queryWrapper = new QueryWrapper<>();
            
            if (StringUtils.isNotBlank(provider)) {
                queryWrapper.eq("provider", provider);
            }
            if (StringUtils.isNotBlank(model)) {
                queryWrapper.eq("model", model);
            }
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            if (StringUtils.isNotBlank(startTime)) {
                queryWrapper.ge("gmt_create", startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                queryWrapper.le("gmt_create", endTime);
            }
            
            // 先查询总数（不包含ORDER BY）
            Long total = usageMapper.selectCount(queryWrapper);
            
            // 为分页查询添加排序
            queryWrapper.orderByDesc("gmt_create");
            
            // 分页查询
            Page<OneapiTokenUsageDO> pageParam = new Page<>(page, pageSize);
            Page<OneapiTokenUsageDO> pageResult = usageMapper.selectPage(pageParam, queryWrapper);
            
            // 构建返回结果
            return OneapiMultiResult.success(pageResult.getRecords(), total);
        } catch (Exception e) {
            log.error("查询访问日志异常", e);
            return OneapiMultiResult.fail("查询访问日志异常：" + e.getMessage());
        }
    }
}
