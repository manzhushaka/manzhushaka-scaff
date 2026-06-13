package com.manzhushaka.mq.core;

import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.springframework.stereotype.Component;

/**
 * 定义 LedgeredRedisStreamPublisher。
 */
@Component
public class LedgeredRedisStreamPublisher {

    private final MqMessageLedgerService ledgerService;
    private final RedisStreamPublisher redisStreamPublisher;

    /**
     * 创建 LedgeredRedisStreamPublisher 实例。
     *
     * @param ledgerService ledgerService 参数
     * @param redisStreamPublisher redisStreamPublisher 参数
     */
    public LedgeredRedisStreamPublisher(MqMessageLedgerService ledgerService, RedisStreamPublisher redisStreamPublisher) {
        this.ledgerService = ledgerService;
        this.redisStreamPublisher = redisStreamPublisher;
    }

    /**
     * 发布消息。
     *
     * @param streamKey streamKey 参数
     * @param event event 参数
     */
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
