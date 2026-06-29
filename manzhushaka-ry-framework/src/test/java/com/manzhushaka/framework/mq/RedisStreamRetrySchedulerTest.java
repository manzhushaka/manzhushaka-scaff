package com.manzhushaka.framework.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RetryScheduler 测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RedisStreamRetrySchedulerTest
{

    @Test
    void retrySchedulerShouldRequeueDueMessages()
    {
        RedisStreamGateway gateway = mock(RedisStreamGateway.class);
        RedisStreamMessageHandler handler = mock(RedisStreamMessageHandler.class);
        when(handler.retryStreamKey()).thenReturn("mq:retry:test");
        when(handler.streamKey()).thenReturn("mq:stream:test");
        when(handler.messageType()).thenReturn("test");
        RedisStreamMessageHandlerRegistry registry =
                new RedisStreamMessageHandlerRegistry(Collections.singletonList(handler));
        Map<String, String> body = new HashMap<>();
        body.put("messageType", "test");
        body.put("businessKey", "BIZ-1");
        body.put("payload", "{}");
        body.put("retryTimes", "1");
        body.put("nextRetryTime", String.valueOf(System.currentTimeMillis() - 1000L));
        when(gateway.range("mq:retry:test", 100)).thenReturn(Collections.singletonList(
                new RedisStreamRecord("mq:retry:test", "1-0", body)));
        RedisStreamRetryScheduler scheduler = new RedisStreamRetryScheduler(gateway, registry);

        scheduler.scanRetryStreams();

        verify(gateway).add(eq("mq:stream:test"), anyMap());
        verify(gateway).delete("mq:retry:test", "1-0");
    }
}