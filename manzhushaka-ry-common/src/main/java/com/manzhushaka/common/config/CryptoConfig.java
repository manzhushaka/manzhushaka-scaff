package com.manzhushaka.common.config;

import com.manzhushaka.common.crypto.AesGcmSensitiveFieldEncryptor;
import com.manzhushaka.common.crypto.CryptoProperties;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感字段加密配置。
 *
 * @author manzhushaka
 */
@Configuration
@EnableConfigurationProperties(CryptoProperties.class)
public class CryptoConfig {

    /**
     * 创建敏感字段加密器。
     *
     * @param properties 加密配置
     * @return 敏感字段加密器
     */
    @Bean
    @ConditionalOnProperty(prefix = "manzhushaka.crypto", name = "enabled", havingValue = "true")
    public SensitiveFieldEncryptor sensitiveFieldEncryptor(CryptoProperties properties) {
        return new AesGcmSensitiveFieldEncryptor(properties);
    }
}