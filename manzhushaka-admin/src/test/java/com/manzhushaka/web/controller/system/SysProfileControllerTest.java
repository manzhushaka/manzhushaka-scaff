package com.manzhushaka.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.application.command.UpdateOwnPasswordCommand;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.web.dto.system.user.UpdateOwnPasswordRequest;

/**
 * 个人信息控制器测试。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
class SysProfileControllerTest
{
    /**
     * 清理 Spring Security 上下文。
     */
    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    /**
     * 修改密码应通过应用服务边界处理。
     */
    @Test
    void updatePwdShouldDelegateToApplicationService()
    {
        SysProfileController controller = new SysProfileController();
        SystemUserAppService userAppService = mock(SystemUserAppService.class);
        ReflectionTestUtils.setField(controller, "userAppService", userAppService);
        ReflectionTestUtils.setField(controller, "tokenService", mock(TokenService.class));
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(2L)
                .username("zhangsan")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
        UpdateOwnPasswordRequest request = new UpdateOwnPasswordRequest();
        request.setOldPassword("Old@7294");
        request.setNewPassword("New@7294");

        assertThat(controller.updatePwd(request).isSuccess()).isTrue();
        verify(userAppService).updateOwnPassword(new UpdateOwnPasswordCommand(
                2L, "zhangsan", "Old@7294", "New@7294"));
    }
}
