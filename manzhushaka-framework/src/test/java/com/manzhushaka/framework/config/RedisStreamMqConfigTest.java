package com.manzhushaka.framework.config;

import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamMessageHandlerRegistry;
import com.manzhushaka.framework.mq.RedisStreamMessageListenerRegistrar;
import com.manzhushaka.framework.mq.RedisStreamRetryScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisStreamMqConfigTest {

    private final RedisStreamMqConfig config = new RedisStreamMqConfig();

    @Test
    void shouldCreateRegistryRegistrarAndRetrySchedulerBeans() {
        RedisStreamMessageHandler handler = mock(RedisStreamMessageHandler.class);
        RedisStreamGateway gateway = mock(RedisStreamGateway.class);
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                mock(StreamMessageListenerContainer.class);

        RedisStreamMessageHandlerRegistry registry = config.redisStreamMessageHandlerRegistry(List.of(handler));
        RedisStreamMessageListenerRegistrar registrar =
                config.redisStreamMessageListenerRegistrar(container, registry, gateway);
        RedisStreamRetryScheduler retryScheduler = config.redisStreamRetryScheduler(gateway, registry);

        assertThat(registry.listHandlers()).containsExactly(handler);
        assertThat(registrar).isNotNull();
        assertThat(retryScheduler).isNotNull();
    }
}
