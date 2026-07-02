package com.manzhushaka.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.exception.user.UserPasswordNotMatchException;
import com.manzhushaka.common.exception.user.UserPasswordRetryLimitExceedException;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.framework.security.context.AuthenticationContextHolder;

/**
 * 登录密码方法
 * 
 * @author manzhushaka
 */
@Component
public class SysPasswordService
{
    @Autowired
    private RedisCache redisCache;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    /**
     * 校验用户登录密码。
     *
     * @param user 用户信息
     */
    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        validatePassword(username, password, user.getPassword());
    }

    /**
     * 校验密码是否匹配。
     *
     * @param user 用户信息
     * @param rawPassword 明文密码
     * @return true 表示匹配
     */
    public boolean matches(SysUser user, String rawPassword)
    {
        return PasswordUtils.matches(rawPassword, user.getPassword());
    }

    /**
     * 校验密码（接收加密密码字符串，不依赖 SysUser 实体）。
     *
     * @param encodedPassword 数据库中存储的加密密码
     */
    public void validate(String encodedPassword)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        validatePassword(username, password, encodedPassword);
    }

    /**
     * 清理登录密码错误记录。
     *
     * @param loginName 登录账号
     */
    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
    }

    /**
     * 按错误次数策略校验登录密码。
     *
     * @param username 用户名
     * @param password 明文密码
     * @param encodedPassword 加密密码
     */
    private void validatePassword(String username, String password, String encodedPassword)
    {
        Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!PasswordUtils.matches(password, encodedPassword))
        {
            retryCount = retryCount + 1;
            redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            if (retryCount >= maxRetryCount)
            {
                throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
            }
            throw new UserPasswordNotMatchException();
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }
}
