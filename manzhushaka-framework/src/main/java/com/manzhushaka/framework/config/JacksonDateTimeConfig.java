package com.manzhushaka.framework.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一系统对外 JSON 的日期时间序列化格式。
 */
@Configuration
public class JacksonDateTimeConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 配置 Jackson 将 LocalDateTime 序列化为标准日期时间字符串。
     *
     * @return Jackson 自定义构建器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomizer() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.modules(javaTimeModule);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        };
    }
}
