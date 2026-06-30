package com.manzhushaka.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;

/**
 * AES-GCM 敏感字段加密器。
 *
 * @author manzhushaka
 */
public class AesGcmSensitiveFieldEncryptor implements SensitiveFieldEncryptor {

    private static final String VERSION_PREFIX = "v1:";
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec aesKeySpec;
    private final SecretKeySpec hmacKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmSensitiveFieldEncryptor(CryptoProperties properties) {
        byte[] aesKey = decodeKey(properties.getAesKey(), "AES");
        byte[] hmacKey = decodeKey(properties.getHmacKey(), "HMAC");
        this.aesKeySpec = new SecretKeySpec(aesKey, AES_ALGORITHM);
        this.hmacKeySpec = new SecretKeySpec(hmacKey, HMAC_ALGORITHM);
    }

    @Override
    public String encrypt(String plaintext) {
        if (StringUtils.isBlank(plaintext) || isCiphertext(plaintext)) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encrypt sensitive field failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (StringUtils.isBlank(ciphertext) || !isCiphertext(ciphertext)) {
            return ciphertext;
        }
        String[] parts = ciphertext.split(":", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid sensitive field ciphertext format");
        }
        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Decrypt sensitive field failed", e);
        }
    }

    @Override
    public String hash(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            return plaintext;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKeySpec);
            byte[] digest = mac.doFinal(plaintext.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hash sensitive field failed", e);
        }
    }

    @Override
    public boolean isCiphertext(String value) {
        return StringUtils.startsWith(value, VERSION_PREFIX);
    }

    private byte[] decodeKey(String key, String keyName) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException(keyName + " key must not be blank");
        }
        byte[] decoded = Base64.getDecoder().decode(key);
        if (decoded.length < 32) {
            throw new IllegalArgumentException(keyName + " key must be at least 256 bits");
        }
        return decoded;
    }

    AesGcmSensitiveFieldEncryptor() {
        this.aesKeySpec = null;
        this.hmacKeySpec = null;
    }
}