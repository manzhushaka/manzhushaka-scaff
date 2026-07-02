package com.manzhushaka.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysUserService;

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
     * 修改密码时不能使用弱密码。
     */
    @Test
    void updatePwdShouldRejectWeakPassword()
    {
        SysProfileController controller = new SysProfileController();
        ISysUserService userService = mock(ISysUserService.class);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "tokenService", mock(TokenService.class));
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(2L)
                .username("zhangsan")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
        SysUser user = new SysUser();
        user.setPassword(PasswordUtils.encrypt("Old@7294"));
        when(userService.selectUserById(2L)).thenReturn(user);

        AjaxResult result = controller.updatePwd(Map.of(
                "oldPassword", "Old@7294",
                "newPassword", "123456"));

        assertThat(result.isError()).isTrue();
        assertThat(result.get(AjaxResult.MSG_TAG)).isEqualTo("密码长度必须介于8到20个字符之间");
        verify(userService, never()).resetUserPwd(anyLong(), anyString());
    }
}
