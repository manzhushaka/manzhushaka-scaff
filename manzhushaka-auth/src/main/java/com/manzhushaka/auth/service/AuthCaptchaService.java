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
    private static final String CAPTCHA_RATE_LIMIT_PREFIX = "auth:captcha:limit:";
    private static final String LOGIN_FAILURE_PREFIX = "auth:login:failure:";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final Duration CAPTCHA_RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final Duration LOGIN_FAILURE_WINDOW = Duration.ofMinutes(5);
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);
    private static final int CAPTCHA_RATE_LIMIT = 10;
    private static final int LOGIN_FAILURE_LIMIT = 5;

    private final StringRedisTemplate redisTemplate;

    public AuthCaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成图片验证码并写入 Redis。
     *
     * @return 验证码响应
     */
    public CaptchaResponse createCaptcha() {
        assertCaptchaRateLimit("global");
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(160, 48, 4, 20);
        String key = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(buildRedisKey(key), captcha.getCode(), CAPTCHA_TTL);

        CaptchaResponse response = new CaptchaResponse();
        response.setKey(key);
        response.setImageBase64(captcha.getImageBase64Data());
        return response;
    }

    /**
     * 校验验证码并在校验后立即失效。
     *
     * @param key 验证码键
     * @param code 用户输入的验证码
     */
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

    /**
     * 校验指定主体是否已被登录失败次数锁定。
     *
     * @param principal 限流主体
     */
    public void assertLoginAllowed(String principal) {
        String normalizedPrincipal = normalizePrincipal(principal);
        String lockKey = buildLoginLockKey(normalizedPrincipal);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BizException(429, "登录失败次数过多，请 15 分钟后再试");
        }
    }

    /**
     * 记录一次登录失败并在达到阈值时触发临时锁定。
     *
     * @param principal 限流主体
     */
    public void recordLoginFailure(String principal) {
        String normalizedPrincipal = normalizePrincipal(principal);
        Long failures = redisTemplate.opsForValue().increment(buildLoginFailureKey(normalizedPrincipal));
        redisTemplate.expire(buildLoginFailureKey(normalizedPrincipal), LOGIN_FAILURE_WINDOW);
        if (failures != null && failures >= LOGIN_FAILURE_LIMIT) {
            redisTemplate.opsForValue().set(buildLoginLockKey(normalizedPrincipal), "1", LOGIN_LOCK_DURATION);
            redisTemplate.delete(buildLoginFailureKey(normalizedPrincipal));
        }
    }

    /**
     * 清理指定主体的登录失败计数与锁定状态。
     *
     * @param principal 限流主体
     */
    public void clearLoginFailures(String principal) {
        String normalizedPrincipal = normalizePrincipal(principal);
        redisTemplate.delete(buildLoginFailureKey(normalizedPrincipal));
        redisTemplate.delete(buildLoginLockKey(normalizedPrincipal));
    }

    /**
     * 获取验证码 Redis 键。
     *
     * @param key 验证码键
     * @return Redis 键
     */
    private String buildRedisKey(String key) {
        return CAPTCHA_KEY_PREFIX + key;
    }

    /**
     * 校验验证码生成频率限制。
     *
     * @param principal 限流主体
     */
    private void assertCaptchaRateLimit(String principal) {
        String key = CAPTCHA_RATE_LIMIT_PREFIX + normalizePrincipal(principal);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, CAPTCHA_RATE_LIMIT_WINDOW);
        }
        if (count != null && count > CAPTCHA_RATE_LIMIT) {
            throw new BizException(429, "验证码请求过于频繁，请稍后再试");
        }
    }

    /**
     * 构造登录失败计数键。
     *
     * @param principal 限流主体
     * @return Redis 键
     */
    private String buildLoginFailureKey(String principal) {
        return LOGIN_FAILURE_PREFIX + principal;
    }

    /**
     * 构造登录锁定键。
     *
     * @param principal 限流主体
     * @return Redis 键
     */
    private String buildLoginLockKey(String principal) {
        return LOGIN_FAILURE_PREFIX + "lock:" + principal;
    }

    /**
     * 标准化限流主体。
     *
     * @param principal 原始主体
     * @return 标准化后的主体
     */
    private String normalizePrincipal(String principal) {
        return StringUtils.hasText(principal) ? principal.trim().toLowerCase() : "anonymous";
    }
}
