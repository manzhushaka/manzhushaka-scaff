package com.manzhushaka.system.infrastructure.persistence.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SysUserSensitiveFieldSupportTest {

    @AfterEach
    void tearDown() {
        SensitiveFieldCryptoHolder.clear();
    }

    @Test
    void shouldPopulateEmailHash() {
        SensitiveFieldCryptoHolder.setEncryptor(new StubEncryptor());
        SysUser user = new SysUser();
        user.setEmail("user@example.com");

        SysUserSensitiveFieldSupport.fillHashes(user);

        assertEquals("user@example.com", user.getEmail());
        assertEquals("hash:user@example.com", user.getEmailHash());
    }

    @Test
    void shouldPopulatePhoneHash() {
        SensitiveFieldCryptoHolder.setEncryptor(new StubEncryptor());
        SysUser user = new SysUser();
        user.setPhonenumber("13800138000");

        SysUserSensitiveFieldSupport.fillHashes(user);

        assertEquals("13800138000", user.getPhonenumber());
        assertEquals("hash:13800138000", user.getPhonenumberHash());
    }

    private static class StubEncryptor implements SensitiveFieldEncryptor {
        @Override
        public String encrypt(String plaintext) {
            return "v1:" + plaintext;
        }

        @Override
        public String decrypt(String ciphertext) {
            return ciphertext;
        }

        @Override
        public String hash(String plaintext) {
            return "hash:" + plaintext;
        }

        @Override
        public boolean isCiphertext(String value) {
            return value != null && value.startsWith("v1:");
        }
    }
}