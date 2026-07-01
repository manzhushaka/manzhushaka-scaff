package com.manzhushaka.common.crypto;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 敏感字段加密器静态访问入口。
 *
 * @author manzhushaka
 */
@Component
public class SensitiveFieldCryptoHolder implements InitializingBean {

    private static SensitiveFieldEncryptor encryptor;

    private final SensitiveFieldEncryptor injectedEncryptor;

    public SensitiveFieldCryptoHolder(@Nullable SensitiveFieldEncryptor injectedEncryptor) {
        this.injectedEncryptor = injectedEncryptor;
    }

    @Override
    public void afterPropertiesSet() {
        encryptor = injectedEncryptor;
    }

    /**
     * 设置加密器（主要用于测试时注入 mock）。
     *
     * @param sensitiveFieldEncryptor 敏感字段加密器
     */
    public static void setEncryptor(SensitiveFieldEncryptor sensitiveFieldEncryptor) {
        encryptor = sensitiveFieldEncryptor;
    }

    /**
     * 清除加密器（主要用于测试清理）。
     */
    public static void clear() {
        encryptor = null;
    }

    /**
     * 加密明文。
     *
     * @param plaintext 明文
     * @return 密文
     */
    public static String encrypt(String plaintext) {
        return activeEncryptor().encrypt(plaintext);
    }

    /**
     * 解密密文。
     *
     * @param ciphertext 密文
     * @return 明文
     */
    public static String decrypt(String ciphertext) {
        return activeEncryptor().decrypt(ciphertext);
    }

    /**
     * 计算精确查询摘要。
     *
     * @param plaintext 明文
     * @return 摘要
     */
    public static String hash(String plaintext) {
        return activeEncryptor().hash(plaintext);
    }

    private static SensitiveFieldEncryptor activeEncryptor() {
        return encryptor == null ? NoopSensitiveFieldEncryptor.INSTANCE : encryptor;
    }
}