package com.manzhushaka.framework.web.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.utils.ServletUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.http.UserAgentUtils;
import com.manzhushaka.common.utils.ip.AddressUtils;
import com.manzhushaka.common.utils.ip.IpUtils;
import com.manzhushaka.common.utils.uuid.IdUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

/**
 * token验证处理
 * 
 * @author manzhushaka
 */
@Component
public class TokenService
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     * 
     * @return 用户信息
     */
    public LoginPrincipal getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                return getLoginPrincipalFromCache(userKey);
            }
            catch (Exception e)
            {
                log.error("获取用户信息异常'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginPrincipal loginPrincipal)
    {
        if (StringUtils.isNotNull(loginPrincipal) && StringUtils.isNotEmpty(loginPrincipal.getToken()))
        {
            refreshToken(loginPrincipal);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 创建令牌
     * 
     * @param loginPrincipal 用户信息
     * @return 令牌
     */
    public String createToken(LoginPrincipal loginPrincipal)
    {
        String token = IdUtils.fastUUID();
        loginPrincipal.setToken(token);
        setUserAgent(loginPrincipal);
        refreshToken(loginPrincipal);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginPrincipal.getUsername());
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     * 
     * @param loginPrincipal 登录信息
     * @return 令牌
     */
    public void verifyToken(LoginPrincipal loginPrincipal)
    {
        long expireTime = loginPrincipal.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginPrincipal);
        }
    }

    /**
     * 刷新令牌有效期
     * 
     * @param loginPrincipal 登录信息
     */
    public void refreshToken(LoginPrincipal loginPrincipal)
    {
        loginPrincipal.setLoginTime(System.currentTimeMillis());
        loginPrincipal.setExpireTime(loginPrincipal.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginPrincipal缓存
        String userKey = getTokenKey(loginPrincipal.getToken());
        redisCache.setCacheObject(userKey, loginPrincipal, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 设置用户代理信息
     * 
     * @param loginPrincipal 登录信息
     */
    public void setUserAgent(LoginPrincipal loginPrincipal)
    {
        String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        String ip = IpUtils.getIpAddr();
        loginPrincipal.setIpaddr(ip);
        loginPrincipal.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginPrincipal.setBrowser(UserAgentUtils.getBrowser(userAgent));
        loginPrincipal.setOs(UserAgentUtils.getOperatingSystem(userAgent));
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    /**
     * 角色权限变更后，刷新所有持有该角色的在线用户权限
     *
     * @param roleId            变更的角色ID
     * @param permissionService 权限服务
     */
    public void refreshPermissionByRoleId(Long roleId, SysPermissionService permissionService)
    {
        // 扫描所有在线 token
        String pattern = CacheConstants.LOGIN_TOKEN_KEY + "*";
        Collection<String> keys = redisCache.keys(pattern);
        if (keys == null || keys.isEmpty())
        {
            return;
        }
        for (String key : keys)
        {
            LoginPrincipal loginPrincipal = getLoginPrincipalFromCache(key);
            if (loginPrincipal == null || loginPrincipal.isAdmin())
            {
                // 管理员拥有所有权限，跳过
                continue;
            }
            // 判断该用户是否拥有此角色
            boolean hasRole = loginPrincipal.getRoleIds() != null
                    && loginPrincipal.getRoleIds().contains(roleId);
            if (!hasRole)
            {
                continue;
            }
            // 刷新权限缓存 - 需要重新查询用户的完整信息以更新权限
            // 通过 userId 查询用户并刷新权限
            com.manzhushaka.system.infrastructure.persistence.entity.SysUser sysUser = new com.manzhushaka.system.infrastructure.persistence.entity.SysUser();
            sysUser.setUserId(loginPrincipal.getUserId());
            loginPrincipal.setPermissions(permissionService.getMenuPermission(sysUser));
            refreshToken(loginPrincipal);
            log.info("角色[{}]权限变更，已刷新在线用户[{}]的权限缓存", roleId, loginPrincipal.getUsername());
        }
    }

    /**
     * 从缓存中恢复登录主体
     *
     * @param userKey 用户缓存键
     * @return 登录主体；不存在或无法恢复时返回 null
     */
    private LoginPrincipal getLoginPrincipalFromCache(String userKey)
    {
        Object cachedUser = redisCache.getCacheObject(userKey);
        return LoginPrincipal.restore(cachedUser);
    }
}
