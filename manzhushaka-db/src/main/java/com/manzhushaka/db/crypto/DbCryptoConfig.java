package com.manzhushaka.db.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 DbCryptoConfig。
 */
@Configuration
@EnableConfigurationProperties(DbCryptoProperties.class)
public class DbCryptoConfig {

    /**
     * 执行 db Field Crypto Service 逻辑。
     *
     * @param properties properties 参数
     * @return 处理结果
     */
    @Bean
    public DbFieldCryptoService dbFieldCryptoService(DbCryptoProperties properties) {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService(properties.getKey());
        EncryptStringTypeHandler.setCryptoService(cryptoService);
        return cryptoService;
    }
}
