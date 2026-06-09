package com.manzhushaka.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.mq.properties.MqProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqProperties.class)
public class MqConfig {

    @Bean
    public ObjectMapper mqObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
