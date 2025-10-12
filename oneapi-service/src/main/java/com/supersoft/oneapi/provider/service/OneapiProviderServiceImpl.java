package com.supersoft.oneapi.provider.service;

import com.alibaba.fastjson.JSON;
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
import com.supersoft.oneapi.util.BooleanUtils;
import com.supersoft.oneapi.util.OneapiCommonUtils;
import com.supersoft.oneapi.util.OneapiProviderModelUtils;
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
                lastModified.set(new Date());
                providerCache.invalidateAll();
                log.info("检测到配置更新, 清除缓存");
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private SlidingWindowMetrics getWindowMetrics(String provider, String model) {
        String key = (provider == null ? "null" : provider) + ":" + (model == null ? "null" : model);
        return metricsMap.computeIfAbsent(key, s -> new SlidingWindowMetrics(provider, model));
    }

    @Override
    public OneapiProvider selectProvider(String modelName, List<OneapiProvider> exclude) {
        return selectProvider(null, modelName, exclude);
    }

    @Override
    public OneapiProvider selectProvider(String provider, String modelName, List<OneapiProvider> exclude) {
        Map<String, List<OneapiProvider>> map = getAvailableProviderMap(provider, modelName);
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        List<OneapiProvider> providerItems;
        if (StringUtils.isNotBlank(provider)) {
            providerItems = map.get(provider);
        } else {
            // 当provider为null时，将所有provider提供的List合并
            providerItems = map.values().stream()
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
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

    /**
     * 获取可用的服务提供者映射
     *
     * @param targetProvider 指定的服务提供者
     * @param modelName      模型名称
     * @return 服务提供者映射
     */
    private Map<String, List<OneapiProvider>> getAvailableProviderMap(String targetProvider, String modelName) {
        // 缓存中获取
        Map<String, List<OneapiProvider>> cache = providerCache.getIfPresent(modelName);
        if (cache != null) {
            return cache;
        }
        QueryWrapper<OneapiProviderDO> query = new QueryWrapper<>();
        query.eq("enable", BooleanUtils.toInteger(true));
        List<OneapiProviderDO> list = oneapiProviderMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return Map.of();
        }
        QueryWrapper<OneapiAccountDO> queryAccount = new QueryWrapper<>();
        queryAccount.eq("status", BooleanUtils.toInteger(true));
        List<OneapiAccountDO> accounts = oneapiAccountMapper.selectList(queryAccount);
        if (CollectionUtils.isEmpty(accounts)) {
            return Map.of();
        }
        Map<String, List<OneapiAccountDO>> accountMap = accounts.stream()
                .collect(Collectors.groupingBy(OneapiAccountDO::getProviderCode));
        Map<String, List<OneapiProvider>> map = new HashMap<>();
        // 遍历服务提供者list，并按照账号展开，给出可以提供服务的所有账号
        list.forEach(item -> {
            String provider = item.getCode();
            // 如果指定了服务提供者则必须使用该服务提供者的账号
            if (StringUtils.isNotBlank(targetProvider) && !targetProvider.equals(provider)) {
                return;
            }
            List<OneapiAccountDO> providerAccounts = accountMap.get(provider);
            if (CollectionUtils.isEmpty(providerAccounts)) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者{}未提供可用的账号", provider);
                }
                return;
            }
            String mapModelName = OneapiProviderModelUtils.parseAndProcessModelMapping(item.getModels(), modelName);
            if (StringUtils.isBlank(mapModelName)) {
                if (OneapiCommonUtils.enableLog()) {
                    log.info("提供者{}未提供{}的模型", provider, modelName);
                }
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
        providerCache.put(modelName, map);
        return map;
    }
}