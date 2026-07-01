package com.manzhushaka.common.crypto;

import org.apache.commons.lang3.StringUtils;

/**
 * 未启用敏感字段加密时的保护性加密器。
 *
 * @author manzhushaka
 */
public enum NoopSensitiveFieldEncryptor implements SensitiveFieldEncryptor {
    /** 单例 */
    INSTANCE;

    @Override
    public String encrypt(String plaintext) {
        if (StringUtils.isBlank(plaintext) || isCiphertext(plaintext)) {
            return plaintext;
        }
        throw new IllegalStateException("Sensitive field crypto is disabled");
    }

    @Override
    public String decrypt(String ciphertext) {
        return ciphertext;
    }

    @Override
    public String hash(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            return plaintext;
        }
        return null;
    }

    @Override
    public boolean isCiphertext(String value) {
        return StringUtils.startsWith(value, "v1:");
    }
}