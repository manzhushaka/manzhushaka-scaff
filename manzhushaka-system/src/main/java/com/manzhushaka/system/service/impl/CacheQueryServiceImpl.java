package com.manzhushaka.system.service.impl;

import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.system.dto.cache.CacheEntryQuery;
import com.manzhushaka.system.service.CacheQueryService;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class CacheQueryServiceImpl implements CacheQueryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int PREVIEW_TEXT_LIMIT = 160;
    private static final int COLLECTION_PREVIEW_LIMIT = 3;
    private static final int COLLECTION_DETAIL_LIMIT = 100;

    private final StringRedisTemplate redisTemplate;

    public CacheQueryServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<CacheEntryVO> listEntries(CacheEntryQuery query) {
        String keyword = query == null ? null : query.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int limit = normalizeLimit(query == null ? null : query.getLimit());
        List<String> keys = scanKeys(buildPattern(keyword), limit);
        List<CacheEntryVO> results = new ArrayList<>(keys.size());
        for (String key : keys) {
            CacheEntryVO entry = buildSummary(key);
            if (entry != null) {
                results.add(entry);
            }
        }
        return results;
    }

    @Override
    public CacheEntryDetailVO getEntryDetail(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BizException(400, "缓存 Key 不能为空");
        }
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new BizException(404, "缓存 Key 不存在或已过期");
        }
        return buildDetail(key);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<String> scanKeys(String pattern, int limit) {
        return redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> keys = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(Math.max(limit * 2L, 50L))
                .build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext() && keys.size() < limit) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception exception) {
                throw new BizException(500, "扫描缓存 Key 失败");
            }
            keys.sort(String::compareTo);
            return keys;
        });
    }

    private CacheEntryVO buildSummary(String key) {
        DataType type = redisTemplate.type(key);
        if (type == null || type == DataType.NONE) {
            return null;
        }
        CacheEntryVO entry = new CacheEntryVO();
        fillBaseFields(entry, key, type);
        entry.setValuePreview(buildPreview(key, type));
        return entry;
    }

    private CacheEntryDetailVO buildDetail(String key) {
        DataType type = redisTemplate.type(key);
        if (type == null || type == DataType.NONE) {
            throw new BizException(404, "缓存 Key 不存在或已过期");
        }
        CacheEntryDetailVO detail = new CacheEntryDetailVO();
        fillBaseFields(detail, key, type);
        Object value = readValue(key, type, COLLECTION_DETAIL_LIMIT);
        detail.setValuePreview(buildPreview(key, type));
        detail.setValue(value);
        return detail;
    }

    private void fillBaseFields(CacheEntryVO target, String key, DataType type) {
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        target.setKey(key);
        target.setType(type.code());
        target.setTtlSeconds(ttlSeconds);
        target.setExpireAt(resolveExpireAt(ttlSeconds));
    }

    private LocalDateTime resolveExpireAt(Long ttlSeconds) {
        if (ttlSeconds == null || ttlSeconds < 0) {
            return null;
        }
        return LocalDateTime.now().plusSeconds(ttlSeconds);
    }

    private String buildPattern(String keyword) {
        String normalized = keyword.trim();
        if (normalized.contains("*") || normalized.contains("?") || normalized.contains("[")) {
            return normalized;
        }
        return "*" + normalized + "*";
    }

    private String buildPreview(String key, DataType type) {
        return switch (type) {
            case STRING -> truncate(asText(redisTemplate.opsForValue().get(key)));
            case HASH -> truncate(buildHashPreview(key));
            case LIST -> truncate(buildListPreview(key));
            case SET -> truncate(buildSetPreview(key));
            case ZSET -> truncate(buildZsetPreview(key));
            default -> "暂不支持该类型预览";
        };
    }

    private String buildHashPreview(String key) {
        Map<String, String> sample = readHashEntries(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForHash().size(key);
        return "Hash(" + defaultLong(size) + ") " + joinPreviewPairs(sample);
    }

    private String buildListPreview(String key) {
        List<String> sample = defaultList(redisTemplate.opsForList().range(key, 0, COLLECTION_PREVIEW_LIMIT - 1));
        Long size = redisTemplate.opsForList().size(key);
        return "List(" + defaultLong(size) + ") " + sample;
    }

    private String buildSetPreview(String key) {
        List<String> sample = readSetMembers(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForSet().size(key);
        return "Set(" + defaultLong(size) + ") " + sample;
    }

    private String buildZsetPreview(String key) {
        List<Map<String, Object>> sample = readZsetMembers(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForZSet().zCard(key);
        return "ZSet(" + defaultLong(size) + ") " + sample;
    }

    private Object readValue(String key, DataType type, int limit) {
        return switch (type) {
            case STRING -> asText(redisTemplate.opsForValue().get(key));
            case HASH -> readHashEntries(key, limit);
            case LIST -> defaultList(redisTemplate.opsForList().range(key, 0, limit - 1));
            case SET -> readSetMembers(key, limit);
            case ZSET -> readZsetMembers(key, limit);
            default -> "暂不支持该类型详情";
        };
    }

    private Map<String, String> readHashEntries(String key, int limit) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        ScanOptions options = ScanOptions.scanOptions().count(limit).build();
        try (Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(key, options)) {
            while (cursor.hasNext() && result.size() < limit) {
                Map.Entry<Object, Object> entry = cursor.next();
                result.put(asText(entry.getKey()), asText(entry.getValue()));
            }
        } catch (Exception exception) {
            throw new BizException(500, "读取 Hash 缓存失败");
        }
        return result;
    }

    private List<String> readSetMembers(String key, int limit) {
        List<String> result = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().count(limit).build();
        try (Cursor<String> cursor = redisTemplate.opsForSet().scan(key, options)) {
            while (cursor.hasNext() && result.size() < limit) {
                result.add(asText(cursor.next()));
            }
        } catch (Exception exception) {
            throw new BizException(500, "读取 Set 缓存失败");
        }
        result.sort(String::compareTo);
        return result;
    }

    private List<Map<String, Object>> readZsetMembers(String key, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().count(limit).build();
        try (Cursor<TypedTuple<String>> cursor = redisTemplate.opsForZSet().scan(key, options)) {
            while (cursor.hasNext() && result.size() < limit) {
                TypedTuple<String> tuple = cursor.next();
                LinkedHashMap<String, Object> item = new LinkedHashMap<>();
                item.put("member", asText(tuple == null ? null : tuple.getValue()));
                item.put("score", tuple == null ? null : tuple.getScore());
                result.add(item);
            }
        } catch (Exception exception) {
            throw new BizException(500, "读取 ZSet 缓存失败");
        }
        return result;
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String joinPreviewPairs(Map<String, String> sample) {
        if (sample.isEmpty()) {
            return "{}";
        }
        List<String> parts = new ArrayList<>(sample.size());
        for (Map.Entry<String, String> entry : sample.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
        }
        return parts.toString();
    }

    private String asText(Object value) {
        return Objects.toString(value, "");
    }

    private String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return "--";
        }
        if (value.length() <= PREVIEW_TEXT_LIMIT) {
            return value;
        }
        return value.substring(0, PREVIEW_TEXT_LIMIT) + "...";
    }
}
