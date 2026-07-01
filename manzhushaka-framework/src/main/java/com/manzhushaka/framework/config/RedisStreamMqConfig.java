package com.manzhushaka.framework.config;

import java.time.Duration;

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
}