package com.manzhushaka.framework.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonDateTimeConfigTest {

    /**
     * 验证全局 Jackson 配置会将 LocalDateTime 输出为统一的日期时间字符串。
     *
     * @throws Exception 序列化异常
     */
    @Test
    void shouldSerializeLocalDateTimeAsStandardDateTimeText() throws Exception {
        JacksonDateTimeConfig config = new JacksonDateTimeConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        config.jacksonObjectMapperCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        String actual = objectMapper.writeValueAsString(new DateTimePayload(LocalDateTime.of(2026, 6, 9, 0, 28, 46)));

        assertEquals("{\"createTime\":\"2026-06-09 00:28:46\"}", actual);
    }

    /**
     * 验证全局 Jackson 配置会拒绝未声明字段，避免静默吞掉异常输入。
     */
    @Test
    void shouldFailOnUnknownProperties() {
        JacksonDateTimeConfig config = new JacksonDateTimeConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        config.jacksonObjectMapperCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        org.junit.jupiter.api.Assertions.assertTrue(
            objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        );
    }

    /**
     * 用于验证 LocalDateTime 序列化效果的测试载体。
     *
     * @param createTime 创建时间
     */
    private record DateTimePayload(LocalDateTime createTime) {
    }
}
