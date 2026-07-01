package com.manzhushaka.common.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AesGcmSensitiveFieldEncryptorTest {

    private static final String AES_KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    private static final String HMAC_KEY = Base64.getEncoder()
            .encodeToString("abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8));

    @Test
    void shouldEncryptAndDecryptValue() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        String ciphertext = encryptor.encrypt("13800138000");

        assertTrue(ciphertext.startsWith("v1:"));
        assertEquals("13800138000", encryptor.decrypt(ciphertext));
    }

    @Test
    void shouldUseRandomIvForSamePlainText() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        String first = encryptor.encrypt("user@example.com");
        String second = encryptor.encrypt("user@example.com");

        assertNotEquals(first, second);
        assertEquals("user@example.com", encryptor.decrypt(first));
        assertEquals("user@example.com", encryptor.decrypt(second));
    }

    @Test
    void shouldGenerateStableHash() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        assertEquals(encryptor.hash("user@example.com"), encryptor.hash("user@example.com"));
        assertNotEquals(encryptor.hash("user@example.com"), encryptor.hash("other@example.com"));
    }

    @Test
    void shouldRejectBlankKey() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey("");
        properties.setHmacKey(HMAC_KEY);

        assertThrows(IllegalArgumentException.class, () -> new AesGcmSensitiveFieldEncryptor(properties));
    }
}