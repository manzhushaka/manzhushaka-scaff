package com.manzhushaka.db.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesDbFieldCryptoServiceTest {

    private static final String BASE64_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void encryptsAndDecryptsStringValues() {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService(BASE64_KEY);

        String ciphertext = cryptoService.encrypt("13800138000");

        assertNotEquals("13800138000", ciphertext);
        assertEquals("13800138000", cryptoService.decrypt(ciphertext));
    }

    @Test
    void usesRandomIvForSamePlaintext() {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService(BASE64_KEY);

        String firstCiphertext = cryptoService.encrypt("sensitive-value");
        String secondCiphertext = cryptoService.encrypt("sensitive-value");

        assertNotEquals(firstCiphertext, secondCiphertext);
    }

    @Test
    void keepsNullValueUntouched() {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService(BASE64_KEY);

        assertNull(cryptoService.encrypt(null));
        assertNull(cryptoService.decrypt(null));
    }

    @Test
    void rejectsBlankKey() {
        DbFieldCryptoService cryptoService = new AesDbFieldCryptoService("");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> cryptoService.encrypt("value"));

        assertEquals("数据库字段加密密钥未配置，请设置 manzhushaka.db.crypto.key", exception.getMessage());
    }
}
