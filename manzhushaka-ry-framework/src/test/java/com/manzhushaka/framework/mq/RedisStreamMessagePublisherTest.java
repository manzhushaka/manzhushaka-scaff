package com.manzhushaka.framework.mq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis Stream 消息发布器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RedisStreamMessagePublisherTest
{

    /**
     * 发布消息时应写入标准字段。
     */
    @Test
    void publishShouldWriteStandardFields()
    {
        RedisStreamGateway gateway = mock(RedisStreamGateway.class);
        RedisStreamMessagePublisher publisher = new RedisStreamMessagePublisher(gateway);
        when(gateway.add(eq("mq:stream:order_paid"), org.mockito.ArgumentMatchers.anyMap())).thenReturn("168-0");

        String messageId = publisher.publish("mq:stream:order_paid", "order_paid", "ORDER-100", "{\"orderId\":100}");

        assertThat(messageId).isEqualTo("168-0");
        org.mockito.ArgumentCaptor<java.util.Map<String, String>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
        verify(gateway).add(eq("mq:stream:order_paid"), captor.capture());
        assertThat(captor.getValue())
                .containsEntry("messageType", "order_paid")
                .containsEntry("businessKey", "ORDER-100")
                .containsEntry("payload", "{\"orderId\":100}")
                .containsEntry("retryTimes", "0");
    }
}