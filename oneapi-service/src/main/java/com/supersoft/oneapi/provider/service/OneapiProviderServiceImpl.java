package com.supersoft.oneapi.provider.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.supersoft.oneapi.provider.data.OneapiAccountDO;
import com.supersoft.oneapi.provider.data.OneapiProviderDO;
import com.supersoft.oneapi.provider.mapper.OneapiAccountMapper;
import com.supersoft.oneapi.provider.mapper.OneapiModelMapper;
import com.supersoft.oneapi.provider.mapper.OneapiProviderMapper;
import com.supersoft.oneapi.provider.model.OneapiProvider;
import com.supersoft.oneapi.provider.qos.SlidingWindowMetrics;
import com.supersoft.oneapi.proxy.model.OneapiProviderQos;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import com.supersoft.oneapi.util.OneapiConfigUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OneapiProviderServiceImpl implements OneapiProviderService {
    private static final Logger qosLogger = LoggerFactory.getLogger("qosLogger");

    private static final Map<String, SlidingWindowMetrics> metricsMap = new ConcurrentHashMap<>();
    private static final Cache<String, Map<String, List<OneapiProvider>>> providerCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(60, TimeUnit.MINUTES).build();
    private static final Cache<String, Set<String>> modelsCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(60, TimeUnit.MINUTES).build();
    // 上次更新时间
    private static final AtomicReference<Date> lastModified = new AtomicReference<>(new Date());

    @Resource
    OneapiProviderMapper oneapiProviderMapper;
    @Resource
    OneapiAccountMapper oneapiAccountMapper;
    @Resource
    OneapiModelMapper oneapiModelMapper;

    @PostConstruct
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> metricsMap.forEach((key, value) -> {
            OneapiProviderQos providerQos = new OneapiProviderQos();
            providerQos.setProvider(value.getProvider());
            providerQos.setModel(value.getModel());
            providerQos.setOneMinuteRt(value.getRt());
            providerQos.setOneMinuteTps(value.getOneMinuteRate());
            providerQos.setOneMinuteSuccessTps(value.getOneMinuteSuccessRate());
            providerQos.setCount(value.getTotalCount());
            providerQos.setSuccessCount(value.getSuccessCount());
            providerQos.setSuccessRate((double) value.getSuccessCount() /value.getTotalCount());
            qosLogger.info(JSON.toJSONString(providerQos));
        }), 1, 1, TimeUnit.MINUTES);
        // 每10s钟刷新一次, 判断模型和账户有无更新
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            Date accountUpdated = oneapiAccountMapper.lastModifiedTime();
            Date providerUpdated = oneapiProviderMapper.lastModifiedTime();
            if (accountUpdated.compareTo(lastModified.get()) > 0 ||
                    providerUpdated.compareTo(lastModified.get()) > 0) {
                log.info("提供者或账户更新, 重新加载提供者缓存, 上次更新时间={}, 最新account更新时间={}, 最新provider更新时间={}",
                        lastModified.get(), accountUpdated, providerUpdated);
                providerCache.invalidateAll();
                lastModified.set(accountUpdated);
            }
            if (providerUpdated.compareTo(lastModified.get()) > 0) {
                log.info("提供者更新, 重新加载提供者缓存");
                modelsCache.invalidateAll();
                lastModified.set(providerUpdated);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }


    @Override
    public OneapiProvider selectProvider(String modelName, List<OneapiProvider> exclude) {
        long count = getWindowMetrics(null, modelName).getTotalCount();
        List<OneapiProvider> providerItems = getAvailableProviderItems(modelName);
        if (CollectionUtils.isEmpty(providerItems)) {
            return null;
        }
        // providerItems - exclude
        if (CollectionUtils.isNotEmpty(exclude)) {
            providerItems.removeIf(exclude::contains);
        }
        if (CollectionUtils.isEmpty(providerItems)) {
            return null;
        }
        // 轮询逻辑
        int index = (int) (count % providerItems.size());
        OneapiProvider providerItem = providerItems.get(index);
        if (OneapiCommonUtils.enableLog()) {
            log.info("选择节点, count: {}, provider: {}, list={}, exclude={}",
                    count, providerItem.getName(), providerItems, exclude);
        }
        // 克隆一次防止对象发生操作
        providerItem = JSON.parseObject(JSON.toJSONString(providerItem), OneapiProvider.class);
        return providerItem;
    }

    private static SlidingWindowMetrics getWindowMetrics(String provider, String model) {
        String finalProvider = StringUtils.isBlank(provider) ? null : provider.toLowerCase();
        String finalModel = StringUtils.isBlank(model) ? null : model.toLowerCase();
        String window;
        if (StringUtils.isNotBlank(finalModel)) {
            window = StringUtils.isBlank(finalProvider) ? model : finalProvider + "." + finalModel;
        } else {
            window = finalProvider;
        }
        return metricsMap.computeIfAbsent(window, (key) -> new SlidingWindowMetrics(finalProvider, finalModel));
    }

    private List<OneapiProvider> getAvailableProviderItems(String modelName) {
        if (StringUtils.isBlank(modelName)) {
            return null;
        }
        modelName = modelName.toLowerCase();
        String [] parts = modelName.split(":");
        String provider;
        if (parts.length > 1) {
            provider = parts[0];
            modelName = parts[1];
        } else {
            provider = null;
        }
        Map<String, List<OneapiProvider>> map = getAvailableProviderMap(provider, modelName);
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        return map.values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public OneapiProvider selectProvider(String provider, String modelName, List<OneapiProvider> exclude) {
        Map<String, List<OneapiProvider>> map = getAvailableProviderMap(provider, modelName);
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        List<OneapiProvider> providerItems = map.get(provider);
        if (CollectionUtils.isEmpty(providerItems)) {
            return null;
        }
        long count = getWindowMetrics(provider, modelName).getTotalCount();
        // 轮询逻辑
        int index = (int) (count % providerItems.size());
        OneapiProvider providerItem = providerItems.get(index);
        // 克隆一次防止对象发生操作
        providerItem = JSON.parseObject(JSON.toJSONString(providerItem), OneapiProvider.class);
        return providerItem;
    }

    @Override
    public void record(OneapiProvider item, long duration, Boolean success) {
        if (OneapiCommonUtils.enableLog()) {
            log.info("调用记录: {}, 耗时: {}ms, 结果: {}", item, duration, success);
        }
        String model = item.getModel() == null ? null : item.getModel().toLowerCase();
        String provider = item.getName() == null ? null : item.getName().toLowerCase();
        getWindowMetrics(provider, model).recordRequest(duration, TimeUnit.MILLISECONDS, success);
        getWindowMetrics(provider, null).recordRequest(duration, TimeUnit.MILLISECONDS, success);
        getWindowMetrics(null, model).recordRequest(duration, TimeUnit.MILLISECONDS, success);
    }

    private Map<String, List<OneapiProvider>> getAvailableProviderMap(String providerName, String modelName) {
        Set<String> availableModels = getAvailableModels();
        if (CollectionUtils.isEmpty(availableModels)) {
            throw new RuntimeException("没有可用的模型");
        }
        // 如果传入的模型名称不正确则默认使用claude-3-haiku
        modelName = modelName.toLowerCase();
        if (!availableModels.contains(modelName)) {
            throw new RuntimeException("未找到请求的模型" + modelName);
        }
        Map<String, List<OneapiProvider>> providerMap = getCachedProviderMap(providerName, modelName);
        if (MapUtils.isEmpty(providerMap)) {
            return null;
        }
        String finalModelName = modelName;
        providerMap.forEach((provider, items) -> {
            SlidingWindowMetrics metrics = getWindowMetrics(provider, finalModelName);
            long totalCount = metrics.getTotalCount();
            Integer tolerate = OneapiConfigUtils.getCacheConfigWithDef("oneapi.tolerate", 0);
            if (totalCount <= tolerate) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者 {} 模型 {} 无调用记录, 直接通过", provider, finalModelName);
                }
                return;
            }
            // 最近1分钟如果未发生过调用则直接进入
            double oneMinuteRate = metrics.getOneMinuteRate();
            if (oneMinuteRate <= 0) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者 {} 模型 {} 1分钟内无调用记录, 直接通过", provider, finalModelName);
                }
                return;
            }
            // 如果1分钟成功率过低直接排除整个provider
            Integer minSuccessRate = OneapiConfigUtils.getCacheConfigWithDef("oneapi.success.rate", 50);
            int successRate = (int) (metrics.getOneMinuteSuccessRate() * 100 / metrics.getOneMinuteRate());
            if (successRate < minSuccessRate) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者 {} 模型 {} 成功率低于50%, 成功率: {}%", provider, finalModelName, successRate);
                }
                providerMap.put(provider, null);
            }
            // 如果平均rt大于5000毫秒则直接排除整个provider
            double rt = metrics.getRt();
            Integer maxRt = getMaxRt(finalModelName);
            if (rt > maxRt) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者 {} 模型 {} 平均响应时间过长, 平均响应时间: {}ms", provider, finalModelName, rt);
                }
                providerMap.put(provider, null);
            }
            // 排除没钱的账号
            items.removeIf(item -> {
                Double balance = item.getBalance();
                if (balance == null) {
                    return false;
                }
                return balance < 1;
            });
        });
        // 排除没有有效item的provider
        providerMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));
        // 如果一个provider都没有了, 还是原样返回
        if (MapUtils.isEmpty(providerMap)) {
            return getCachedProviderMap(providerName, modelName);
        }
        return providerMap;
    }

    /**
     * 获取指定模型可接受的最大超时时间
     * @param finalModelName
     * @return
     */
    private Integer getMaxRt(String finalModelName) {
        Integer maxRt = OneapiConfigUtils.getCacheConfig("oneapi.success.rt." + finalModelName, Integer.class);
        if (maxRt == null) {
            maxRt = OneapiConfigUtils.getCacheConfigWithDef("oneapi.success.rt", 5000);
        }
        return maxRt;
    }

    public Set<String> getAvailableModels() {
        Set<String> models = modelsCache.getIfPresent("models");
        if (models == null) {
            QueryWrapper<OneapiProviderDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enable", true);
            models = oneapiModelMapper.selectModelNames();
            modelsCache.put("models", models);
        }
        return models;
    }

    private Map<String, List<OneapiProvider>> getCachedProviderMap(String targetProvider, String modelName) {
        Map<String, List<OneapiProvider>> map = providerCache.getIfPresent(modelName);
        if (map == null) {
            map = getProviderMap(targetProvider, modelName);
            providerCache.put(modelName, map);
        }
        return map;
    }

    /**
     * 本函数不做缓存, 在上层调用时做缓存
     * 根据模型名称获取提供者列表
     * 单个服务提供者, 如阿里云可以配置多个账号, 数据量较小直接在内存计算, 避免复杂json sql查询
     * @param modelName
     * @return
     */
    private Map<String, List<OneapiProvider>> getProviderMap(String targetProvider, String modelName) {
        QueryWrapper<OneapiProviderDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enable", true);
        List<OneapiProviderDO> list = oneapiProviderMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Map.of();
        }
        QueryWrapper<OneapiAccountDO> queryAccount = new QueryWrapper<>();
        queryAccount.eq("status", 1);
        List<OneapiAccountDO> accounts = oneapiAccountMapper.selectList(queryAccount);
        if (CollectionUtils.isEmpty(accounts)) {
            return Map.of();
        }
        Map<String, List<OneapiAccountDO>> accountMap = accounts.stream()
                .collect(Collectors.groupingBy(OneapiAccountDO::getName));
        Map<String, List<OneapiProvider>> map = new HashMap<>();
        // 遍历服务提供者list，并按照账号展开，给出可以提供服务的所有账号
        list.forEach(item -> {
            String provider = item.getName();
            // 如果指定了服务提供者则必须使用该服务提供者的账号
            if (StringUtils.isNotBlank(targetProvider) && !targetProvider.equals(provider)) {
                return;
            }
            Map<String, String> modelMap = JSON.parseObject(item.getModels(), new TypeReference<>() {});
            if (MapUtils.isEmpty(modelMap)) {
                log.info("提供者配置异常: {}", item);
                return;
            }
            modelMap = toLowerCaseKeys(modelMap);
            List<OneapiAccountDO> providerAccounts = accountMap.get(provider);
            if (CollectionUtils.isEmpty(providerAccounts)) {
                log.info("提供者{}未提供可用的账号", provider);
                return;
            }
            String mapModelName = modelMap.get(modelName.toLowerCase());
            if (StringUtils.isBlank(mapModelName)) {
                log.debug("提供者{}未提供{}的模型", provider, modelName);
                return;
            }
            // 仅使用有余额的账户
            providerAccounts.stream().filter(account -> account.getBalance() > 0).forEach(account -> {
                OneapiProvider providerItem = new OneapiProvider();
                providerItem.setName(provider);
                providerItem.setType(item.getType());
                providerItem.setNote(account.getNote());
                providerItem.setApi(item.getApi());
                providerItem.setModel(modelName);
                providerItem.setModelMapping(mapModelName);
                providerItem.setKey(account.getApiKey());
                providerItem.setAk(account.getAk());
                providerItem.setSk(account.getSk());
                providerItem.setService(item.getService());
                List<OneapiProvider> items = map.computeIfAbsent(provider, k1 -> new ArrayList<>());
                items.add(providerItem);
            });
        });
        return map;
    }

    public static Map<String, String> toLowerCaseKeys(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            return map;
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
    }
}
