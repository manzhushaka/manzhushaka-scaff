package com.manzhushaka.framework.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.framework.security.model.LoginPrincipal;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Token 服务测试
 *
 * @author manzhushaka
 */
class TokenServiceTest {

    /**
     * 缓存返回 JSONObject 时应恢复为登录主体
     */
    @Test
    void getLoginUserShouldRestoreLoginPrincipalFromJsonObjectCache() {
        TokenService tokenService = new TokenService();
        RedisCache redisCache = mock(RedisCache.class);
        ReflectionTestUtils.setField(tokenService, "redisCache", redisCache);
        ReflectionTestUtils.setField(tokenService, "header", "Authorization");
        ReflectionTestUtils.setField(tokenService, "secret", "unit-test-secret");

        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(1L)
                .deptId(103L)
                .deptName("研发部门")
                .username("admin")
                .token("token-1")
                .build();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(principal));
        when(redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + "token-1"))
                .thenReturn(jsonObject);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", Constants.TOKEN_PREFIX + buildToken("unit-test-secret", "token-1", "admin"));

        LoginPrincipal actual = tokenService.getLoginUser(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getDeptId()).isEqualTo(103L);
        assertThat(actual.getDeptName()).isEqualTo("研发部门");
        assertThat(actual.getUsername()).isEqualTo("admin");
        assertThat(actual.getToken()).isEqualTo("token-1");
    }

    /**
     * 构建测试用 JWT
     *
     * @param secret 测试密钥
     * @param token 登录 token
     * @param username 用户名
     * @return JWT 字符串
     */
    private String buildToken(String secret, String token, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, username);
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
