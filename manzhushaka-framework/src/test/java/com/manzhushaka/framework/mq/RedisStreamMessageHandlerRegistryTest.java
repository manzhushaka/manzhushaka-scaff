package com.manzhushaka.framework.mq;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Registry 测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RedisStreamMessageHandlerRegistryTest
{

    @Test
    void registryShouldRejectDuplicateMessageType()
    {
        TestHandler first = new TestHandler("order_paid", "mq:stream:order_paid");
        TestHandler second = new TestHandler("order_paid", "mq:stream:order_paid_v2");

        assertThatThrownBy(() -> new RedisStreamMessageHandlerRegistry(Arrays.asList(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("order_paid");
    }

    @Test
    void registryShouldFindHandlerByStreamKey()
    {
        TestHandler handler = new TestHandler("order_paid", "mq:stream:order_paid");
        RedisStreamMessageHandlerRegistry registry =
                new RedisStreamMessageHandlerRegistry(Collections.singletonList(handler));

        assertThat(registry.getByStreamKey("mq:stream:order_paid")).isSameAs(handler);
    }

    private static class TestHandler extends AbstractRedisStreamMessageHandler
    {
        private final String type;
        private final String stream;

        TestHandler(String type, String stream)
        {
            super(null, null);
            this.type = type;
            this.stream = stream;
        }

        @Override
        public String messageType() { return type; }

        @Override
        public String streamKey() { return stream; }

        @Override
        public String consumerGroup() { return "mq-group-" + type; }

        @Override
        public String consumerName() { return "mq-consumer-" + type; }

        @Override
        protected void doHandle(RedisStreamRecord record) {}
    }
}