package com.manzhushaka.db.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定义 DbCryptoProperties。
 */
@ConfigurationProperties(prefix = "manzhushaka.db.crypto")
public class DbCryptoProperties {
    private String key;

    /**
     * 返回 key。
     *
     * @return 字段值
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置 key。
     *
     * @param key 键名
     */
    public void setKey(String key) {
        this.key = key;
    }
}
