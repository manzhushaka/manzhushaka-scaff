package com.manzhushaka.framework.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SensitiveDataSanitizerTest {

    @Test
    void shouldMaskSensitiveFieldsRecursively() {
        Map<String, Object> payload = Map.of(
            "username", "admin",
            "password", "secret",
            "profile", Map.of(
                "mobile", "13812345678",
                "token", "abc-token"
            )
        );

        Object masked = SensitiveDataSanitizer.mask(payload);

        Map<?, ?> maskedMap = assertInstanceOf(Map.class, masked);
        assertEquals("***", maskedMap.get("password"));
        Map<?, ?> profile = assertInstanceOf(Map.class, maskedMap.get("profile"));
        assertEquals("138****5678", profile.get("mobile"));
        assertEquals("***", profile.get("token"));
    }
}
