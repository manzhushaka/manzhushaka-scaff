package com.manzhushaka.web.controller.common;

import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.framework.captcha.CaptchaImage;
import com.manzhushaka.framework.captcha.CaptchaService;
import com.manzhushaka.system.service.ISysConfigService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证码控制器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class CaptchaControllerTest
{
    /**
     * 验证码开启时应返回 uuid、图片并缓存验证码文本。
     */
    @Test
    void getCodeShouldReturnCaptchaImageAndCacheCode()
    {
        CaptchaController controller = new CaptchaController();
        RedisCache redisCache = mock(RedisCache.class);
        ISysConfigService configService = mock(ISysConfigService.class);
        CaptchaService captchaService = mock(CaptchaService.class);
        ReflectionTestUtils.setField(controller, "redisCache", redisCache);
        ReflectionTestUtils.setField(controller, "configService", configService);
        ReflectionTestUtils.setField(controller, "captchaService", captchaService);
        when(configService.selectCaptchaEnabled()).thenReturn(true);
        when(captchaService.createCharCaptcha()).thenReturn(new CaptchaImage("A7K9", Base64.getEncoder().encodeToString(
                new byte[] { 1, 2, 3 })));

        AjaxResult ajax = controller.getCode();

        assertThat(ajax.get("captchaEnabled")).isEqualTo(true);
        assertThat(ajax.get("uuid")).isInstanceOf(String.class);
        assertThat((String) ajax.get("uuid")).isNotBlank();
        assertThat(ajax.get("img")).isEqualTo("AQID");
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisCache).setCacheObject(keyCaptor.capture(), eq("A7K9"), eq(Constants.CAPTCHA_EXPIRATION),
                eq(TimeUnit.MINUTES));
        assertThat(keyCaptor.getValue()).startsWith(CacheConstants.CAPTCHA_CODE_KEY);
    }

    /**
     * 验证码关闭时不应生成 uuid、图片或缓存数据。
     */
    @Test
    void getCodeShouldReturnOnlyFlagWhenCaptchaDisabled()
    {
        CaptchaController controller = new CaptchaController();
        RedisCache redisCache = mock(RedisCache.class);
        ISysConfigService configService = mock(ISysConfigService.class);
        CaptchaService captchaService = mock(CaptchaService.class);
        ReflectionTestUtils.setField(controller, "redisCache", redisCache);
        ReflectionTestUtils.setField(controller, "configService", configService);
        ReflectionTestUtils.setField(controller, "captchaService", captchaService);
        when(configService.selectCaptchaEnabled()).thenReturn(false);

        AjaxResult ajax = controller.getCode();

        assertThat(ajax.get("captchaEnabled")).isEqualTo(false);
        assertThat(ajax).doesNotContainKeys("uuid", "img");
        verify(captchaService, never()).createCharCaptcha();
        verify(redisCache, never()).setCacheObject(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any());
    }
}
