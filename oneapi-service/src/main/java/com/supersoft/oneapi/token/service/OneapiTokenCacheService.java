package com.supersoft.oneapi.token.service;

import com.supersoft.oneapi.common.OneapiSingleResult;
import com.supersoft.oneapi.token.data.OneapiTokenDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌缓存服务
 * 提供1分钟刷新一次的缓存机制，提升API密钥验证性能
 */
@Service
@Slf4j
public class OneapiTokenCacheService {
    
    @Resource
    private OneapiTokenService tokenService;
    
    // 令牌缓存，key为apiKey，value为token信息
    private final ConcurrentHashMap<String, OneapiTokenDO> tokenCache = new ConcurrentHashMap<>();
    
    // 缓存更新版本号，用于检测缓存是否最新
    private final AtomicLong cacheVersion = new AtomicLong(0);
    
    /**
     * 验证API密钥（带缓存）
     * @param apiKey API密钥
     * @return 验证结果
     */
    public OneapiSingleResult<OneapiTokenDO> validateApiKeyWithCache(String apiKey) {
        try {
            // 首先从缓存获取
            OneapiTokenDO cachedToken = tokenCache.get(apiKey);
            if (cachedToken != null) {
                // 使用缓存的令牌进行验证
                return validateCachedToken(cachedToken);
            }
            
            // 缓存未命中，从数据库查询并验证
            OneapiSingleResult<OneapiTokenDO> result = tokenService.validateApiKey(apiKey);
            
            // 如果验证成功，加入缓存
            if (result.isSuccess() && result.getData() != null) {
                tokenCache.put(apiKey, result.getData());
            }
            
            return result;
        } catch (Exception e) {
            log.error("缓存验证API密钥异常", e);
            return OneapiSingleResult.fail("验证异常：" + e.getMessage());
        }
    }
    
    /**
     * 验证缓存中的令牌
     */
    private OneapiSingleResult<OneapiTokenDO> validateCachedToken(OneapiTokenDO token) {
        // 检查令牌状态
        if (token.getStatus() != 1) {
            return OneapiSingleResult.fail("令牌已禁用");
        }
        
        // 检查过期时间
        if (token.getExpireTime() != null && token.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            return OneapiSingleResult.fail("令牌已过期");
        }
        
        // 检查token使用量限制
        if (token.getMaxUsage() != -1 && token.getTokenUsage() >= token.getMaxUsage()) {
            return OneapiSingleResult.fail("令牌token使用量已达上限");
        }
        
        return OneapiSingleResult.success(token);
    }
    
    /**
     * 更新令牌的token使用量（异步更新缓存和数据库）
     * @param apiKey API密钥
     * @param additionalTokens 增加的token数量
     */
    public void updateTokenUsageAsync(String apiKey, Long additionalTokens) {
        try {
            // 异步更新数据库中的token使用量
            new Thread(() -> {
                try {
                    OneapiTokenDO token = tokenCache.get(apiKey);
                    if (token != null) {
                        // 更新缓存中的token使用量
                        synchronized (token) {
                            token.setTokenUsage(token.getTokenUsage() + additionalTokens);
                        }
                        
                        // 更新数据库
                        tokenService.updateToken(token);
                        
                        log.debug("异步更新token使用量完成，apiKey: {}, additionalTokens: {}", 
                                 maskApiKey(apiKey), additionalTokens);
                    }
                } catch (Exception e) {
                    log.error("异步更新token使用量异常，apiKey: {}", maskApiKey(apiKey), e);
                }
            }).start();
        } catch (Exception e) {
            log.error("启动异步更新token使用量任务异常", e);
        }
    }
    
    /**
     * 每分钟刷新一次缓存
     */
    @Scheduled(fixedRate = 60000) // 60秒 = 1分钟
    public void refreshCache() {
        try {
            log.debug("开始刷新令牌缓存...");
            
            // 获取所有有效令牌
            var result = tokenService.getAllTokens();
            if (result.isSuccess() && result.getData() != null) {
                // 清空旧缓存
                tokenCache.clear();
                
                // 重新构建缓存
                for (OneapiTokenDO token : result.getData()) {
                    if (token.getStatus() == 1) { // 只缓存启用的令牌
                        tokenCache.put(token.getApiKey(), token);
                    }
                }
                
                // 更新缓存版本
                cacheVersion.incrementAndGet();
                
                log.debug("令牌缓存刷新完成，缓存数量: {}, 版本: {}", 
                         tokenCache.size(), cacheVersion.get());
            }
        } catch (Exception e) {
            log.error("刷新令牌缓存异常", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("缓存大小: %d, 版本: %d", 
                           tokenCache.size(), cacheVersion.get());
    }
    
    /**
     * 掩码显示API密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return apiKey;
        }
        return apiKey.substring(0, 6) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
