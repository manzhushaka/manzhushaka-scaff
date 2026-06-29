package com.manzhushaka.framework.captcha;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.RandomGenerator;
import org.springframework.stereotype.Service;

/**
 * 图片验证码生成服务。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Service
public class CaptchaService
{
    private static final int IMAGE_WIDTH = 160;
    private static final int IMAGE_HEIGHT = 60;
    private static final int CODE_LENGTH = 4;
    private static final int CIRCLE_COUNT = 20;
    private static final String BASE_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 生成字符验证码。
     *
     * @return 验证码结果
     */
    public CaptchaImage createCharCaptcha()
    {
        AbstractCaptcha captcha = CaptchaUtil.createCircleCaptcha(IMAGE_WIDTH, IMAGE_HEIGHT,
                new RandomGenerator(BASE_CODE_CHARS, CODE_LENGTH), CIRCLE_COUNT);
        return new CaptchaImage(captcha.getCode(), captcha.getImageBase64());
    }
}
