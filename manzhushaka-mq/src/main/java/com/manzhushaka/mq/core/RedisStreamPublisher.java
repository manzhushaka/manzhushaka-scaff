package com.manzhushaka.mq.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 定义 RedisStreamPublisher。
 */
@Component
public class RedisStreamPublisher {
    private static final long STREAM_MAX_LENGTH = 100000L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 创建 RedisStreamPublisher 实例。
     *
     * @param redisTemplate redisTemplate 参数
     * @param mqObjectMapper mqObjectMapper 参数
     */
    public RedisStreamPublisher(StringRedisTemplate redisTemplate, ObjectMapper mqObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = mqObjectMapper;
    }

    /**
     * 发布消息。
     *
     * @param streamKey streamKey 参数
     * @param event event 参数
     */
    public void publish(String streamKey, MqEvent<?> event) {
        try {
            redisTemplate.opsForStream().add(MapRecord.create(streamKey, buildRecordValue(event)));
            redisTemplate.opsForStream().trim(streamKey, STREAM_MAX_LENGTH, true);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("序列化 MQ 事件失败", exception);
        }
    }

    /**
     * 构建 build Record Value 结果。
     *
     * @param event event 参数
     * @return 处理结果
     */
    private Map<String, String> buildRecordValue(MqEvent<?> event) throws JsonProcessingException {
        return Map.of(
            "eventId", event.getEventId(),
            "eventType", event.getEventType(),
            "bizKey", event.getBizKey() == null ? "" : event.getBizKey(),
            "traceId", event.getTraceId() == null ? "" : event.getTraceId(),
            "source", event.getSource() == null ? "" : event.getSource(),
            "retryCount", String.valueOf(event.getRetryCount() == null ? 0 : event.getRetryCount()),
            "occurredAt", String.valueOf(event.getOccurredAt()),
            "payload", objectMapper.writeValueAsString(event.getPayload())
        );
    }
}
