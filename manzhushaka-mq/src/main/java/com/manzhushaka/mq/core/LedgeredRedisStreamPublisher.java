package com.manzhushaka.mq.core;

import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.springframework.stereotype.Component;

@Component
public class LedgeredRedisStreamPublisher {

    private final MqMessageLedgerService ledgerService;
    private final RedisStreamPublisher redisStreamPublisher;

    public LedgeredRedisStreamPublisher(MqMessageLedgerService ledgerService, RedisStreamPublisher redisStreamPublisher) {
        this.ledgerService = ledgerService;
        this.redisStreamPublisher = redisStreamPublisher;
    }

    public void publish(String streamKey, MqEvent<?> event) {
        ledgerService.createInitRecord(streamKey, event);
        try {
            redisStreamPublisher.publish(streamKey, event);
        } catch (RuntimeException exception) {
            ledgerService.markFailed(event.getEventId(), exception.getMessage());
            throw exception;
        }
        ledgerService.markPublished(event.getEventId());
    }
}
