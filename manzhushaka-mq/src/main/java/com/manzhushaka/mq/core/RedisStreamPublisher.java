package com.manzhushaka.mq.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisStreamPublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamPublisher(StringRedisTemplate redisTemplate, ObjectMapper mqObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = mqObjectMapper;
    }

    public void publish(String streamKey, MqEvent<?> event) {
        try {
            redisTemplate.opsForStream().add(MapRecord.create(streamKey, Map.of(
                "eventId", event.getEventId(),
                "eventType", event.getEventType(),
                "bizKey", event.getBizKey() == null ? "" : event.getBizKey(),
                "traceId", event.getTraceId() == null ? "" : event.getTraceId(),
                "source", event.getSource() == null ? "" : event.getSource(),
                "retryCount", String.valueOf(event.getRetryCount() == null ? 0 : event.getRetryCount()),
                "occurredAt", String.valueOf(event.getOccurredAt()),
                "payload", objectMapper.writeValueAsString(event.getPayload())
            )));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("序列化 MQ 事件失败", exception);
        }
    }
}
