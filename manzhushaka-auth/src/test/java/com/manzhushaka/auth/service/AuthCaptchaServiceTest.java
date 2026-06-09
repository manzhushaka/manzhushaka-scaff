package com.manzhushaka.auth.service;

import com.manzhushaka.auth.vo.CaptchaResponse;
import com.manzhushaka.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthCaptchaServiceTest {

    @Test
    void createCaptchaReturnsKeyAndBase64Image() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AuthCaptchaService service = new AuthCaptchaService(redisTemplate);

        CaptchaResponse response = service.createCaptcha();

        assertTrue(response.getKey() != null && !response.getKey().isBlank());
        assertTrue(response.getImageBase64().startsWith("data:image/png;base64,"));
        verify(valueOperations).set(startsWith("auth:captcha:"), any(String.class), eq(Duration.ofMinutes(5)));
    }

    @Test
    void validateRejectsExpiredCaptcha() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:captcha:captcha-key")).thenReturn(null);
        AuthCaptchaService service = new AuthCaptchaService(redisTemplate);

        BizException exception = assertThrows(BizException.class, () -> service.validate("captcha-key", "ABCD"));

        assertEquals("验证码已过期", exception.getMessage());
        verify(redisTemplate).delete("auth:captcha:captcha-key");
    }

    @Test
    void validateRejectsWrongCaptcha() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:captcha:captcha-key")).thenReturn("ABCD");
        AuthCaptchaService service = new AuthCaptchaService(redisTemplate);

        BizException exception = assertThrows(BizException.class, () -> service.validate("captcha-key", "WXYZ"));

        assertEquals("验证码错误", exception.getMessage());
        verify(redisTemplate).delete("auth:captcha:captcha-key");
    }
}
