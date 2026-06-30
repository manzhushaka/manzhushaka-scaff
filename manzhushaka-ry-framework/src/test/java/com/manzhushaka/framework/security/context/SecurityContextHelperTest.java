package com.manzhushaka.framework.security.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.framework.security.model.LoginPrincipal;

/**
 * 安全上下文辅助工具测试
 *
 * @author manzhushaka
 */
class SecurityContextHelperTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * principal 为 JSONObject 时应恢复为登录主体
     */
    @Test
    void getPrincipalShouldRestoreLoginPrincipalFromJsonObject() {
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(1L)
                .deptId(103L)
                .deptName("研发部门")
                .username("admin")
                .token("token-1")
                .build();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(principal));
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(jsonObject, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginPrincipal actual = SecurityContextHelper.getPrincipal();

        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getDeptId()).isEqualTo(103L);
        assertThat(actual.getDeptName()).isEqualTo("研发部门");
        assertThat(actual.getUsername()).isEqualTo("admin");
        assertThat(actual.getToken()).isEqualTo("token-1");
    }
}
