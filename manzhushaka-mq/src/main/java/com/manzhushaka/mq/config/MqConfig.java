package com.manzhushaka.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.mq.properties.MqProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置 MqConfig 相关组件。
 */
@Configuration
@EnableConfigurationProperties(MqProperties.class)
public class MqConfig {

    /**
     * 执行 mq Object Mapper 逻辑。
     *
     * @return 处理结果
     */
    @Bean
    public ObjectMapper mqObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
