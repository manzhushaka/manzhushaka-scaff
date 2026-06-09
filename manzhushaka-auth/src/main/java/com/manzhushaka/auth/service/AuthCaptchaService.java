package com.manzhushaka.auth.service;

import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import com.manzhushaka.auth.vo.CaptchaResponse;
import com.manzhushaka.common.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthCaptchaService {
    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public AuthCaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public CaptchaResponse createCaptcha() {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(160, 48, 4, 20);
        String key = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(buildRedisKey(key), captcha.getCode(), CAPTCHA_TTL);

        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setImageBase64(captcha.getImageBase64Data());
        return response;
    }

    public void validate(String key, String code) {
        String redisKey = buildRedisKey(key);
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);
        if (!StringUtils.hasText(storedCode)) {
            throw new BizException(400, "验证码已过期");
        }
        if (!storedCode.equalsIgnoreCase(code == null ? "" : code.trim())) {
            throw new BizException(400, "验证码错误");
        }
    }

    private String buildRedisKey(String key) {
        return CAPTCHA_KEY_PREFIX + key;
    }
}
