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

/**
 * 实现 CacheQueryServiceImpl 业务服务。
 */
@Service
public class CacheQueryServiceImpl implements CacheQueryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int PREVIEW_TEXT_LIMIT = 160;
    private static final int COLLECTION_PREVIEW_LIMIT = 3;
    private static final int COLLECTION_DETAIL_LIMIT = 100;

    private final StringRedisTemplate redisTemplate;

    /**
     * 创建 CacheQueryServiceImpl 实例。
     *
     * @param redisTemplate redisTemplate 参数
     */
    public CacheQueryServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询 list Entries 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
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

    /**
     * 返回 entryDetail。
     *
     * @param key 键名
     * @return 字段值
     */
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

    /**
     * 构建 normalize Limit 结果。
     *
     * @param limit limit 参数
     * @return 处理结果
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * 执行 scan Keys 逻辑。
     *
     * @param pattern pattern 参数
     * @param limit limit 参数
     * @return 处理结果
     */
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

    /**
     * 构建 build Summary 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
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

    /**
     * 构建 build Detail 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
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

    /**
     * 更新 fill Base Fields 数据。
     *
     * @param target target 参数
     * @param key 键名
     * @param type type 参数
     */
    private void fillBaseFields(CacheEntryVO target, String key, DataType type) {
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        target.setKey(key);
        target.setType(type.code());
        target.setTtlSeconds(ttlSeconds);
        target.setExpireAt(resolveExpireAt(ttlSeconds));
    }

    /**
     * 构建 resolve Expire At 结果。
     *
     * @param ttlSeconds ttlSeconds 参数
     * @return 处理结果
     */
    private LocalDateTime resolveExpireAt(Long ttlSeconds) {
        if (ttlSeconds == null || ttlSeconds < 0) {
            return null;
        }
        return LocalDateTime.now().plusSeconds(ttlSeconds);
    }

    /**
     * 构建 build Pattern 结果。
     *
     * @param keyword keyword 参数
     * @return 处理结果
     */
    private String buildPattern(String keyword) {
        String normalized = keyword.trim();
        if (normalized.contains("*") || normalized.contains("?") || normalized.contains("[")) {
            return normalized;
        }
        return "*" + normalized + "*";
    }

    /**
     * 构建 build Preview 结果。
     *
     * @param key 键名
     * @param type type 参数
     * @return 处理结果
     */
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

    /**
     * 构建 build Hash Preview 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
    private String buildHashPreview(String key) {
        Map<String, String> sample = readHashEntries(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForHash().size(key);
        return "Hash(" + defaultLong(size) + ") " + joinPreviewPairs(sample);
    }

    /**
     * 构建 build List Preview 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
    private String buildListPreview(String key) {
        List<String> sample = defaultList(redisTemplate.opsForList().range(key, 0, COLLECTION_PREVIEW_LIMIT - 1));
        Long size = redisTemplate.opsForList().size(key);
        return "List(" + defaultLong(size) + ") " + sample;
    }

    /**
     * 构建 build Set Preview 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
    private String buildSetPreview(String key) {
        List<String> sample = readSetMembers(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForSet().size(key);
        return "Set(" + defaultLong(size) + ") " + sample;
    }

    /**
     * 构建 build Zset Preview 结果。
     *
     * @param key 键名
     * @return 处理结果
     */
    private String buildZsetPreview(String key) {
        List<Map<String, Object>> sample = readZsetMembers(key, COLLECTION_PREVIEW_LIMIT);
        Long size = redisTemplate.opsForZSet().zCard(key);
        return "ZSet(" + defaultLong(size) + ") " + sample;
    }

    /**
     * 执行 read Value 逻辑。
     *
     * @param key 键名
     * @param type type 参数
     * @param limit limit 参数
     * @return 处理结果
     */
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

    /**
     * 执行 read Hash Entries 逻辑。
     *
     * @param key 键名
     * @param limit limit 参数
     * @return 处理结果
     */
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

    /**
     * 执行 read Set Members 逻辑。
     *
     * @param key 键名
     * @param limit limit 参数
     * @return 处理结果
     */
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

    /**
     * 执行 read Zset Members 逻辑。
     *
     * @param key 键名
     * @param limit limit 参数
     * @return 处理结果
     */
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

    /**
     * 执行 default List 逻辑。
     *
     * @param values values 参数
     * @return 处理结果
     */
    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }

    /**
     * 执行 default Long 逻辑。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 执行 join Preview Pairs 逻辑。
     *
     * @param sample sample 参数
     * @return 处理结果
     */
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

    /**
     * 执行 as Text 逻辑。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private String asText(Object value) {
        return Objects.toString(value, "");
    }

    /**
     * 构建 truncate 结果。
     *
     * @param value 字段值
     * @return 处理结果
     */
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
