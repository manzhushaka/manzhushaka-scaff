package com.manzhushaka.framework.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.exception.user.UserPasswordRetryLimitExceedException;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.framework.security.context.AuthenticationContextHolder;

/**
 * 登录密码服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
class SysPasswordServiceTest
{
    /**
     * 清理线程认证上下文，避免影响其他测试。
     */
    @AfterEach
    void tearDown()
    {
        AuthenticationContextHolder.clearContext();
    }

    /**
     * 第五次密码错误应立即锁定账号 10 分钟。
     */
    @Test
    void shouldLockAccountOnFifthPasswordMismatch()
    {
        SysPasswordService passwordService = buildPasswordService();
        RedisCache redisCache = (RedisCache) ReflectionTestUtils.getField(passwordService, "redisCache");
        String cacheKey = CacheConstants.PWD_ERR_CNT_KEY + "admin";
        when(redisCache.getCacheObject(cacheKey)).thenReturn(4);
        AuthenticationContextHolder.setContext(new UsernamePasswordAuthenticationToken("admin", "wrong"));

        Throwable actual = catchThrowable(() -> passwordService.validate(PasswordUtils.encrypt("Mzs@7294")));

        assertThat(actual.getClass()).isEqualTo(UserPasswordRetryLimitExceedException.class);

        verify(redisCache).setCacheObject(cacheKey, 5, 10, TimeUnit.MINUTES);
    }

    /**
     * 已达到错误上限时应直接拒绝登录。
     */
    @Test
    void shouldRejectLoginWhenAccountAlreadyLocked()
    {
        SysPasswordService passwordService = buildPasswordService();
        RedisCache redisCache = (RedisCache) ReflectionTestUtils.getField(passwordService, "redisCache");
        String cacheKey = CacheConstants.PWD_ERR_CNT_KEY + "admin";
        when(redisCache.getCacheObject(cacheKey)).thenReturn(5);
        AuthenticationContextHolder.setContext(new UsernamePasswordAuthenticationToken("admin", "Mzs@7294"));

        Throwable actual = catchThrowable(() -> passwordService.validate(PasswordUtils.encrypt("Mzs@7294")));

        assertThat(actual.getClass()).isEqualTo(UserPasswordRetryLimitExceedException.class);
    }

    /**
     * 构建带测试配置的密码服务。
     *
     * @return 密码服务
     */
    private SysPasswordService buildPasswordService()
    {
        SysPasswordService passwordService = new SysPasswordService();
        ReflectionTestUtils.setField(passwordService, "redisCache", mock(RedisCache.class));
        ReflectionTestUtils.setField(passwordService, "maxRetryCount", 5);
        ReflectionTestUtils.setField(passwordService, "lockTime", 10);
        return passwordService;
    }

    /**
     * 捕获执行过程中的异常。
     *
     * @param runnable 待执行逻辑
     * @return 捕获到的异常；未抛异常时返回 null
     */
    private Throwable catchThrowable(ThrowingRunnable runnable)
    {
        try
        {
            runnable.run();
            return null;
        }
        catch (Throwable throwable)
        {
            return throwable;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable
    {
        /**
         * 执行可能抛出异常的逻辑。
         *
         * @throws Throwable 执行异常
         */
        void run() throws Throwable;
    }
}
