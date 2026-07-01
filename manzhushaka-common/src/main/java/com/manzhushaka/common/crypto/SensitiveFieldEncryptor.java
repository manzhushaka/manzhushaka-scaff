package com.manzhushaka.common.crypto;

/**
 * 敏感字段加密器。
 *
 * @author manzhushaka
 */
public interface SensitiveFieldEncryptor {

    /**
     * 加密明文。
     *
     * @param plaintext 明文
     * @return 密文
     */
    String encrypt(String plaintext);

    /**
     * 解密密文。
     *
     * @param ciphertext 密文
     * @return 明文
     */
    String decrypt(String ciphertext);

    /**
     * 计算精确查询摘要。
     *
     * @param plaintext 明文
     * @return 摘要
     */
    String hash(String plaintext);

    /**
     * 判断值是否已加密。
     *
     * @param value 待判断值
     * @return 是否密文
     */
    boolean isCiphertext(String value);
}