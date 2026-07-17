package com.manzhushaka.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;
import com.manzhushaka.web.dto.system.UnlockScreenRequest;

/**
 * 系统首页控制器测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class SysIndexControllerTest
{
    private SysIndexController controller;

    private SystemSecurityQueryService systemSecurityQueryService;

    /**
     * 初始化控制器和登录上下文。
     */
    @BeforeEach
    void setUp()
    {
        controller = new SysIndexController();
        systemSecurityQueryService = mock(SystemSecurityQueryService.class);
        ReflectionTestUtils.setField(controller, "systemSecurityQueryService", systemSecurityQueryService);
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(2L)
                .username("zhangsan")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
    }

    /**
     * 清理 Spring Security 上下文。
     */
    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    /**
     * 正确密码应成功解锁。
     */
    @Test
    void unlockScreenShouldUseSecurityApplicationService()
    {
        UnlockScreenRequest request = new UnlockScreenRequest();
        request.setPassword("Correct@7294");
        when(systemSecurityQueryService.matchesPassword("zhangsan", "Correct@7294")).thenReturn(true);

        AjaxResult result = controller.unlockScreen(request);

        assertThat(result.isSuccess()).isTrue();
        verify(systemSecurityQueryService).matchesPassword("zhangsan", "Correct@7294");
    }

    /**
     * 空密码应在查询用户前被拒绝。
     */
    @Test
    void unlockScreenShouldRejectEmptyPassword()
    {
        UnlockScreenRequest request = new UnlockScreenRequest();

        AjaxResult result = controller.unlockScreen(request);

        assertThat(result.isError()).isTrue();
        assertThat(result.get(AjaxResult.MSG_TAG)).isEqualTo("密码不能为空");
        verify(systemSecurityQueryService, never()).matchesPassword("zhangsan", null);
    }
}
