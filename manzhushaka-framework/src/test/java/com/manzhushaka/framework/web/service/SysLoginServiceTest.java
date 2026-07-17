package com.manzhushaka.framework.web.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.system.service.ISysConfigService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-17
 */
class SysLoginServiceTest
{
    /**
     * 验证码关闭时登录校验不应访问验证码缓存。
     */
    @Test
    void validateCaptchaShouldSkipValidationWhenCaptchaIsDisabled()
    {
        SysLoginService loginService = new SysLoginService();
        ISysConfigService configService = mock(ISysConfigService.class);
        RedisCache redisCache = mock(RedisCache.class);
        ReflectionTestUtils.setField(loginService, "configService", configService);
        ReflectionTestUtils.setField(loginService, "redisCache", redisCache);
        when(configService.selectCaptchaEnabled()).thenReturn(false);

        loginService.validateCaptcha("admin", null, null);

        verify(redisCache, never()).getCacheObject(org.mockito.ArgumentMatchers.anyString());
        verify(redisCache, never()).deleteObject(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * 验证码开启时应校验并删除已使用的验证码。
     */
    @Test
    void validateCaptchaShouldValidateAndDeleteCodeWhenCaptchaIsEnabled()
    {
        SysLoginService loginService = new SysLoginService();
        ISysConfigService configService = mock(ISysConfigService.class);
        RedisCache redisCache = mock(RedisCache.class);
        ReflectionTestUtils.setField(loginService, "configService", configService);
        ReflectionTestUtils.setField(loginService, "redisCache", redisCache);
        when(configService.selectCaptchaEnabled()).thenReturn(true);
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + "uuid-1";
        when(redisCache.getCacheObject(verifyKey)).thenReturn("A7K9");

        loginService.validateCaptcha("admin", "a7k9", "uuid-1");

        verify(redisCache).getCacheObject(verifyKey);
        verify(redisCache).deleteObject(verifyKey);
    }
}
