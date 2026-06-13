package com.manzhushaka.db.crypto;

/**
 * 定义 DbFieldCryptoService。
 */
public interface DbFieldCryptoService {
    /**
     * 执行 encrypt 逻辑。
     *
     * @param plaintext plaintext 参数
     * @return 处理结果
     */
    String encrypt(String plaintext);

    /**
     * 执行 decrypt 逻辑。
     *
     * @param ciphertext ciphertext 参数
     * @return 处理结果
     */
    String decrypt(String ciphertext);
}
