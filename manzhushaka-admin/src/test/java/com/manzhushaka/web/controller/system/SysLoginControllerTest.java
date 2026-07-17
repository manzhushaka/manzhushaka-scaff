package com.manzhushaka.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;
import com.manzhushaka.system.service.ISysConfigService;
import com.manzhushaka.web.vo.system.user.AuthUserProfileVO;

/**
 * 登录控制器测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class SysLoginControllerTest
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
     * 当前用户接口应返回展示 VO，不得序列化内部密码摘要。
     *
     * @throws Exception JSON 序列化失败时抛出
     */
    @Test
    void getInfoShouldReturnSanitizedUserProfile() throws Exception
    {
        Set<String> permissions = Set.of("system:user:list");
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(2L)
                .username("zhangsan")
                .permissions(permissions)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));

        AuthUserProfileResult profile = new AuthUserProfileResult(
                2L, 10L, "研发部", "zhangsan", "张三", "/profile/avatar.png",
                "$2a$10$sensitiveHash", "0", "0", false,
                Set.of(3L), Set.of("common"), permissions, new Date());
        SystemSecurityQueryService securityQueryService = mock(SystemSecurityQueryService.class);
        when(securityQueryService.loadAuthProfileByUserId(2L)).thenReturn(profile);

        SysLoginController controller = new SysLoginController();
        ReflectionTestUtils.setField(controller, "systemSecurityQueryService", securityQueryService);
        ReflectionTestUtils.setField(controller, "tokenService", mock(TokenService.class));
        ReflectionTestUtils.setField(controller, "configService", mock(ISysConfigService.class));

        AjaxResult result = controller.getInfo();
        String json = new ObjectMapper().writeValueAsString(result);

        assertThat(result.get("user")).isInstanceOf(AuthUserProfileVO.class);
        assertThat(json).contains("\"avatar\":\"/profile/avatar.png\"");
        assertThat(json).doesNotContain("sensitiveHash", "\"password\"");
    }
}
