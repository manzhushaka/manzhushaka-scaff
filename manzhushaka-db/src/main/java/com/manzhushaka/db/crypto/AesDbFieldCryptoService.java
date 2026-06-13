package com.manzhushaka.db.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 定义 AesDbFieldCryptoService。
 */
public class AesDbFieldCryptoService implements DbFieldCryptoService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH = 12;
    /**
     * 执行 Secure Random 逻辑。
     *
     * @return 处理结果
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String key;

    /**
     * 创建 AesDbFieldCryptoService 实例。
     *
     * @param key 键名
     */
    public AesDbFieldCryptoService(String key) {
        this.key = key;
    }

    /**
     * 执行 encrypt 逻辑。
     *
     * @param plaintext plaintext 参数
     * @return 处理结果
     */
    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, resolveKeySpec(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, payload, IV_LENGTH, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("数据库字段加密失败", exception);
        }
    }

    /**
     * 执行 decrypt 逻辑。
     *
     * @param ciphertext ciphertext 参数
     * @return 处理结果
     */
    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        if (ciphertext.isEmpty()) {
            return "";
        }
        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext);
            if (payload.length <= IV_LENGTH) {
                throw new IllegalStateException("数据库字段解密失败，密文格式非法");
            }
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, resolveKeySpec(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("数据库字段解密失败", exception);
        }
    }

    /**
     * 构建 resolve Key Spec 结果。
     *
     * @return 处理结果
     */
    private SecretKeySpec resolveKeySpec() {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("数据库字段加密密钥未配置，请设置 manzhushaka.db.crypto.key");
        }
        byte[] keyBytes = decodeBase64Key(key);
        if (keyBytes == null) {
            keyBytes = key.getBytes(StandardCharsets.UTF_8);
        }
        if (!isValidKeyLength(keyBytes.length)) {
            throw new IllegalStateException("数据库字段加密密钥长度非法，仅支持 16/24/32 字节原始密钥或对应 Base64 值");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 构建 decode Base64 Key 结果。
     *
     * @param keyValue keyValue 参数
     * @return 处理结果
     */
    private byte[] decodeBase64Key(String keyValue) {
        try {
            byte[] decoded = Base64.getDecoder().decode(keyValue);
            return isValidKeyLength(decoded.length) ? decoded : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * 判断是否 validKeyLength。
     *
     * @param length length 参数
     * @return 字段值
     */
    private boolean isValidKeyLength(int length) {
        return length == 16 || length == 24 || length == 32;
    }
}
