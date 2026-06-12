package com.manzhushaka.mq.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisStreamPublisherTest {

    @Test
    void publishShouldTrimStreamLengthApproximatelyToOneHundredThousand() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        StreamOperations<String, Object, Object> streamOperations = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        RedisStreamPublisher publisher = new RedisStreamPublisher(redisTemplate, new ObjectMapper().findAndRegisterModules());

        publisher.publish("stream:test", buildEvent());

        verify(streamOperations).add(any(MapRecord.class));
        verify(streamOperations).trim("stream:test", 100000L, true);
    }

    private MqEvent<String> buildEvent() {
        MqEvent<String> event = new MqEvent<>();
        event.setEventId("event-001");
        event.setEventType("TEST_EVENT");
        event.setBizKey("biz-001");
        event.setTraceId("trace-001");
        event.setSource("mq-test");
        event.setRetryCount(0);
        event.setOccurredAt(LocalDateTime.of(2026, 6, 12, 10, 0));
        event.setPayload("payload");
        return event;
    }
}
