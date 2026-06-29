package com.manzhushaka.framework.mq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 基于 RedisTemplate 的 Stream 操作实现。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Component
public class RedisStreamGatewayImpl implements RedisStreamGateway
{
    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisStreamGatewayImpl(RedisTemplate<Object, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String add(String streamKey, Map<String, String> body)
    {
        RecordId recordId = redisTemplate.opsForStream().add(streamKey, body);
        return recordId.getValue();
    }

    @Override
    public void acknowledge(String streamKey, String consumerGroup, String messageId)
    {
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, RecordId.of(messageId));
    }

    @Override
    public void createGroupIfAbsent(String streamKey, String consumerGroup)
    {
        try
        {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
        }
        catch (RedisSystemException ex)
        {
            // BUSYGROUP 表示组已存在，忽略
            if (!ex.getMessage().contains("BUSYGROUP"))
            {
                throw ex;
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<RedisStreamRecord> range(String streamKey, int count)
    {
        // 使用 reverseRange 读取最新的 N 条消息
        List records = redisTemplate.opsForStream()
                .reverseRange(streamKey, Range.unbounded());
        if (records == null || records.isEmpty())
        {
            return Collections.emptyList();
        }
        // 取最近 count 条
        int toIndex = Math.min(records.size(), count);
        List subset = records.subList(0, toIndex);

        List<RedisStreamRecord> result = new ArrayList<>();
        for (Object obj : subset)
        {
            MapRecord record = (MapRecord) obj;
            Map value = (Map) record.getValue();
            result.add(new RedisStreamRecord(
                    record.getStream().toString(),
                    record.getId().getValue(),
                    convertBody(value)));
        }
        return result;
    }

    @Override
    public void delete(String streamKey, String messageId)
    {
        redisTemplate.opsForStream().delete(streamKey, RecordId.of(messageId));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, String> convertBody(Map raw)
    {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (Object entryObj : raw.entrySet())
        {
            Map.Entry entry = (Map.Entry) entryObj;
            result.put(entry.getKey() != null ? entry.getKey().toString() : "",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return result;
    }
}