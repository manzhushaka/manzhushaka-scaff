package com.manzhushaka.framework.config;

import java.time.Duration;
import java.util.List;

import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamMessageHandlerRegistry;
import com.manzhushaka.framework.mq.RedisStreamMessageListenerRegistrar;
import com.manzhushaka.framework.mq.RedisStreamRetryScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Redis Stream MQ 配置。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Configuration
@EnableScheduling
public class RedisStreamMqConfig
{
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
            redisStreamMessageListenerContainer(RedisConnectionFactory connectionFactory)
    {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String,
                MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();
        return StreamMessageListenerContainer.create(connectionFactory, options);
    }

    @Bean
    public RedisStreamMessageHandlerRegistry redisStreamMessageHandlerRegistry(
            List<RedisStreamMessageHandler> handlers)
    {
        return new RedisStreamMessageHandlerRegistry(handlers);
    }

    @Bean
    public RedisStreamMessageListenerRegistrar redisStreamMessageListenerRegistrar(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            RedisStreamMessageHandlerRegistry registry,
            RedisStreamGateway gateway)
    {
        return new RedisStreamMessageListenerRegistrar(container, registry, gateway);
    }

    @Bean
    public RedisStreamRetryScheduler redisStreamRetryScheduler(
            RedisStreamGateway gateway,
            RedisStreamMessageHandlerRegistry registry)
    {
        return new RedisStreamRetryScheduler(gateway, registry);
    }
}
