package com.manzhushaka.framework.mq;

import io.lettuce.core.RedisBusyException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Redis Stream 网关实现测试。
 *
 * @author manzhushaka
 * @date 2026-07-03
 */
class RedisStreamGatewayImplTest
{
    /**
     * 已存在消费者组时，Redis 会返回 BUSYGROUP，应视为幂等成功。
     */
    @Test
    void createGroupIfAbsentShouldIgnoreBusyGroupFromNestedCause()
    {
        RedisSystemException exception = new RedisSystemException("Error in execution",
                new RedisBusyException("BUSYGROUP Consumer Group name already exists"));
        RedisStreamGatewayImpl gateway = gatewayThrowing(exception);

        assertThatCode(() -> gateway.createGroupIfAbsent("test:stream", "test-stream-group"))
                .doesNotThrowAnyException();
    }

    /**
     * 非消费者组已存在错误应继续抛出，避免掩盖真实 Redis 故障。
     */
    @Test
    void createGroupIfAbsentShouldRethrowOtherRedisErrors()
    {
        RedisSystemException exception = new RedisSystemException("Error in execution",
                new IllegalStateException("connection broken"));
        RedisStreamGatewayImpl gateway = gatewayThrowing(exception);

        assertThatThrownBy(() -> gateway.createGroupIfAbsent("test:stream", "test-stream-group"))
                .isSameAs(exception);
    }

    @SuppressWarnings("unchecked")
    private RedisStreamGatewayImpl gatewayThrowing(RedisSystemException exception)
    {
        RedisTemplate<Object, Object> redisTemplate = mock(RedisTemplate.class);
        StreamOperations<Object, Object, Object> streamOperations = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        doThrow(exception).when(streamOperations).createGroup("test:stream", "test-stream-group");
        return new RedisStreamGatewayImpl(redisTemplate);
    }
}
