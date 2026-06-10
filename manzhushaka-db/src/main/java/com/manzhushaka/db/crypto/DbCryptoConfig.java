package com.manzhushaka.db.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DbCryptoProperties.class)
public class DbCryptoConfig {

    @Bean
    public DbFieldCryptoService dbFieldCryptoService(DbCryptoProperties properties) {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService(properties.getKey());
        EncryptStringTypeHandler.setCryptoService(cryptoService);
        return cryptoService;
    }
}
