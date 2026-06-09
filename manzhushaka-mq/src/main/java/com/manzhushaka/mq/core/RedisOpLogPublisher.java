package com.manzhushaka.mq.core;

import com.manzhushaka.common.model.OpLogRecord;
import com.manzhushaka.common.spi.OpLogPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class RedisOpLogPublisher implements OpLogPublisher {
    private final LedgeredRedisStreamPublisher streamPublisher;

    public RedisOpLogPublisher(LedgeredRedisStreamPublisher streamPublisher) {
        this.streamPublisher = streamPublisher;
    }

    @Override
    public void publish(OpLogRecord record) {
        MqEvent<OpLogRecord> event = new MqEvent<>();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType("OP_LOG_CREATED");
        event.setBizKey(record.getTraceId());
        event.setOccurredAt(LocalDateTime.now());
        event.setTraceId(record.getTraceId());
        event.setSource("manzhushaka-framework");
        event.setRetryCount(0);
        event.setPayload(record);
        streamPublisher.publish(MqStreams.OP_LOG, event);
    }
}
