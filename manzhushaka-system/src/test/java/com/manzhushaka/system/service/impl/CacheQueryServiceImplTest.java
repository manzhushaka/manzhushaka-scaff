package com.manzhushaka.system.service.impl;

import com.manzhushaka.system.dto.cache.CacheEntryQuery;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheQueryServiceImplTest {

    @Test
    void shouldListStringCacheEntryWithTtlAndPreview() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            return callback.doInRedis(connection);
        });
        when(connection.scan(any(ScanOptions.class))).thenReturn(new StaticCursor<>(List.of(bytes("auth:captcha:test"))));
        when(redisTemplate.type("auth:captcha:test")).thenReturn(DataType.STRING);
        when(redisTemplate.getExpire("auth:captcha:test", TimeUnit.SECONDS)).thenReturn(120L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:captcha:test")).thenReturn("ABCD");

        CacheQueryServiceImpl service = new CacheQueryServiceImpl(redisTemplate);
        CacheEntryQuery query = new CacheEntryQuery();
        query.setKeyword("captcha");
        query.setLimit(20);

        List<CacheEntryVO> entries = service.listEntries(query);

        assertEquals(1, entries.size());
        assertEquals("auth:captcha:test", entries.get(0).getKey());
        assertEquals("string", entries.get(0).getType());
        assertEquals(120L, entries.get(0).getTtlSeconds());
        assertEquals("ABCD", entries.get(0).getValuePreview());
    }

    @Test
    void shouldReturnStringCacheEntryDetail() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.hasKey("auth:captcha:test")).thenReturn(true);
        when(redisTemplate.type("auth:captcha:test")).thenReturn(DataType.STRING);
        when(redisTemplate.getExpire("auth:captcha:test", TimeUnit.SECONDS)).thenReturn(120L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:captcha:test")).thenReturn("ABCD");

        CacheQueryServiceImpl service = new CacheQueryServiceImpl(redisTemplate);

        CacheEntryDetailVO detail = service.getEntryDetail("auth:captcha:test");

        assertEquals("auth:captcha:test", detail.getKey());
        assertEquals("string", detail.getType());
        assertEquals(120L, detail.getTtlSeconds());
        assertEquals("ABCD", detail.getValuePreview());
        assertEquals("ABCD", detail.getValue());
        assertNotNull(detail.getExpireAt());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static final class StaticCursor<T> implements Cursor<T> {
        private final Iterator<T> iterator;
        private long position;
        private boolean closed;

        private StaticCursor(List<T> items) {
            this.iterator = items.iterator();
        }

        @Override
        public CursorId getId() {
            return CursorId.initial();
        }

        @Override
        public long getCursorId() {
            return 0L;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public long getPosition() {
            return position;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public boolean hasNext() {
            return !closed && iterator.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            position++;
            return iterator.next();
        }
    }
}
