package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;
import com.manzhushaka.biz.pii.application.service.BiCacheService;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.common.core.redis.RedisCache;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
public class BiCacheServiceImpl implements BiCacheService {
    private static final String DASHBOARD_PREFIX = "pii:bi:dashboard:";
    private static final String DEPT_PREFIX = "pii:bi:dept:";

    private final RedisCache redisCache;
    private final PiiProperties properties;

    public BiCacheServiceImpl(RedisCache redisCache, PiiProperties properties) {
        this.redisCache = redisCache;
        this.properties = properties;
    }

    @Override
    public BiDashboardResult getDashboard(BiDashboardQuery query) {
        return redisCache.getCacheObject(DASHBOARD_PREFIX + hash(query));
    }

    @Override
    public void putDashboard(BiDashboardQuery query, BiDashboardResult result) {
        redisCache.setCacheObject(DASHBOARD_PREFIX + hash(query), result, properties.getBi().getCacheSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public BiDeptAggregateResult getDeptAggregate(String level, Long parentDeptId, BiDashboardQuery query) {
        return redisCache.getCacheObject(DEPT_PREFIX + hash(level + "|" + parentDeptId + "|" + query));
    }

    @Override
    public void putDeptAggregate(String level, Long parentDeptId, BiDashboardQuery query, BiDeptAggregateResult result) {
        redisCache.setCacheObject(DEPT_PREFIX + hash(level + "|" + parentDeptId + "|" + query), result,
                properties.getBi().getCacheSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void evictAll() {
        deleteByPattern(DASHBOARD_PREFIX + "*");
        deleteByPattern(DEPT_PREFIX + "*");
    }

    private void deleteByPattern(String pattern) {
        Collection<String> keys = redisCache.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisCache.deleteObject(keys);
        }
    }

    private String hash(Object value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
