package com.manzhushaka.mq.core;

import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LedgeredRedisStreamPublisherTest {

    @Test
    void publishShouldCreateInitRecordBeforeRedisWriteAndMarkPublishedAfterwards() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        LedgeredRedisStreamPublisher publisher = new LedgeredRedisStreamPublisher(ledgerService, redisStreamPublisher);
        MqEvent<String> event = buildEvent();

        publisher.publish("stream:test", event);

        InOrder inOrder = inOrder(ledgerService, redisStreamPublisher);
        inOrder.verify(ledgerService).createInitRecord("stream:test", event);
        inOrder.verify(redisStreamPublisher).publish("stream:test", event);
        inOrder.verify(ledgerService).markPublished(event.getEventId());
        verify(ledgerService, never()).markFailed(eq(event.getEventId()), anyString());
    }

    @Test
    void publishShouldMarkFailedWhenRedisWriteThrowsException() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        LedgeredRedisStreamPublisher publisher = new LedgeredRedisStreamPublisher(ledgerService, redisStreamPublisher);
        MqEvent<String> event = buildEvent();
        IllegalStateException expected = new IllegalStateException("redis stream write failed");
        doThrow(expected).when(redisStreamPublisher).publish("stream:test", event);

        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> publisher.publish("stream:test", event));

        assertSame(expected, actual);
        InOrder inOrder = inOrder(ledgerService, redisStreamPublisher);
        inOrder.verify(ledgerService).createInitRecord("stream:test", event);
        inOrder.verify(redisStreamPublisher).publish("stream:test", event);
        inOrder.verify(ledgerService).markFailed(event.getEventId(), "redis stream write failed");
        verify(ledgerService, never()).markPublished(event.getEventId());
    }

    @Test
    void publishShouldNotMarkFailedWhenMarkPublishedThrowsException() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        LedgeredRedisStreamPublisher publisher = new LedgeredRedisStreamPublisher(ledgerService, redisStreamPublisher);
        MqEvent<String> event = buildEvent();
        IllegalStateException expected = new IllegalStateException("mark published failed");
        doThrow(expected).when(ledgerService).markPublished(event.getEventId());

        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> publisher.publish("stream:test", event));

        assertSame(expected, actual);
        InOrder inOrder = inOrder(ledgerService, redisStreamPublisher);
        inOrder.verify(ledgerService).createInitRecord("stream:test", event);
        inOrder.verify(redisStreamPublisher).publish("stream:test", event);
        inOrder.verify(ledgerService).markPublished(event.getEventId());
        verify(ledgerService, never()).markFailed(eq(event.getEventId()), anyString());
    }

    private MqEvent<String> buildEvent() {
        MqEvent<String> event = new MqEvent<>();
        event.setEventId("event-001");
        event.setEventType("TEST_EVENT");
        event.setBizKey("biz-001");
        event.setTraceId("trace-001");
        event.setSource("mq-test");
        event.setRetryCount(0);
        event.setOccurredAt(LocalDateTime.of(2026, 6, 9, 10, 0));
        event.setPayload("payload");
        return event;
    }
}
