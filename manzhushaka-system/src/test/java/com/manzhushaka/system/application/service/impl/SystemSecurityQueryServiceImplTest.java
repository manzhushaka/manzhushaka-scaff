package com.manzhushaka.system.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 系统安全认证查询服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class SystemSecurityQueryServiceImplTest
{
    private SystemSecurityQueryServiceImpl service;

    private ISysUserService userService;

    /**
     * 初始化被测服务及依赖。
     */
    @BeforeEach
    void setUp()
    {
        service = new SystemSecurityQueryServiceImpl();
        userService = mock(ISysUserService.class);
        ReflectionTestUtils.setField(service, "userService", userService);
    }

    /**
     * 密码匹配时应返回 true，错误密码应返回 false。
     */
    @Test
    void matchesPasswordShouldValidateStoredHash()
    {
        SysUser user = new SysUser();
        user.setPassword(PasswordUtils.encrypt("Correct@7294"));
        when(userService.selectUserByUserName("zhangsan")).thenReturn(user);

        assertThat(service.matchesPassword("zhangsan", "Correct@7294")).isTrue();
        assertThat(service.matchesPassword("zhangsan", "Wrong@7294")).isFalse();
    }

    /**
     * 空用户名或空密码不应访问数据库。
     */
    @Test
    void matchesPasswordShouldRejectBlankInputBeforeQuery()
    {
        assertThat(service.matchesPassword("", "password")).isFalse();
        assertThat(service.matchesPassword("zhangsan", null)).isFalse();
        verifyNoInteractions(userService);
    }

    /**
     * 认证资料应携带前端展示头像。
     */
    @Test
    void loadAuthProfileShouldCarryAvatar()
    {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("admin");
        user.setAvatar("/profile/avatar.png");
        when(userService.selectUserById(1L)).thenReturn(user);

        AuthUserProfileResult profile = service.loadAuthProfileByUserId(1L);

        assertThat(profile.avatar()).isEqualTo("/profile/avatar.png");
    }
}
