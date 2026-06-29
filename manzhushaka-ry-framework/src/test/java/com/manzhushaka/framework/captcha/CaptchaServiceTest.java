package com.manzhushaka.framework.captcha;

import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 图片验证码生成服务测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class CaptchaServiceTest
{
    private static final Pattern CODE_PATTERN = Pattern.compile("[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}");

    /**
     * 字符验证码应返回 4 位易读字符和非空图片。
     */
    @Test
    void createCharCaptchaShouldReturnReadableCodeAndImage()
    {
        CaptchaService captchaService = new CaptchaService();

        CaptchaImage captchaImage = captchaService.createCharCaptcha();

        assertThat(captchaImage.code()).matches(CODE_PATTERN);
        assertThat(Base64.getDecoder().decode(captchaImage.imageBase64())).isNotEmpty();
    }
}
