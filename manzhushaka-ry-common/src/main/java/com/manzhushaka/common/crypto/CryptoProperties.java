package com.manzhushaka.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 敏感字段加密配置。
 *
 * @author manzhushaka
 */
@ConfigurationProperties(prefix = "manzhushaka.crypto")
public class CryptoProperties {

    /** 是否启用敏感字段加密 */
    private boolean enabled = false;

    /** Base64 编码的 AES 密钥 */
    private String aesKey;

    /** Base64 编码的 HMAC 密钥 */
    private String hmacKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public void setHmacKey(String hmacKey) {
        this.hmacKey = hmacKey;
    }
}