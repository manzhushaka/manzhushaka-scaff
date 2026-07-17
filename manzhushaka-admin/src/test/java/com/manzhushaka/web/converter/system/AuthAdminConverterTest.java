package com.manzhushaka.web.converter.system;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.manzhushaka.framework.web.command.LoginCommand;
import com.manzhushaka.framework.web.command.RegisterCommand;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.web.dto.system.LoginRequest;
import com.manzhushaka.web.dto.system.RegisterRequest;
import com.manzhushaka.web.vo.system.user.AuthUserProfileVO;

/**
 * AuthAdminConverter 单元测试
 *
 * @author manzhushaka
 */
class AuthAdminConverterTest
{
    @Test
    @DisplayName("LoginRequest -> LoginCommand 字段完整映射")
    void testToLoginCommand_ShouldMapAllFields()
    {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");
        request.setCode("ABC");
        request.setUuid("uuid-123");

        // when
        LoginCommand command = AuthAdminConverter.toLoginCommand(request);

        // then
        assertNotNull(command);
        assertEquals("admin", command.username());
        assertEquals("password123", command.password());
        assertEquals("ABC", command.code());
        assertEquals("uuid-123", command.uuid());
    }

    @Test
    @DisplayName("LoginRequest 为 null 时返回 null")
    void testToLoginCommand_WithNull_ShouldReturnNull()
    {
        assertNull(AuthAdminConverter.toLoginCommand(null));
    }

    @Test
    @DisplayName("RegisterRequest -> RegisterCommand 字段完整映射")
    void testToRegisterCommand_ShouldMapAllFields()
    {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("pass456");
        request.setCode("DEF");
        request.setUuid("uuid-456");

        // when
        RegisterCommand command = AuthAdminConverter.toRegisterCommand(request);

        // then
        assertNotNull(command);
        assertEquals("newuser", command.username());
        assertEquals("pass456", command.password());
        assertEquals("DEF", command.code());
        assertEquals("uuid-456", command.uuid());
    }

    @Test
    @DisplayName("RegisterRequest 为 null 时返回 null")
    void testToRegisterCommand_WithNull_ShouldReturnNull()
    {
        assertNull(AuthAdminConverter.toRegisterCommand(null));
    }

    /**
     * 当前用户响应只包含前端展示字段，不得泄露内部认证信息。
     *
     * @throws Exception JSON 序列化失败时抛出
     */
    @Test
    @DisplayName("认证资料转换后不暴露密码和权限集合")
    void toAuthUserProfileVOShouldExposeOnlyDisplayFields() throws Exception
    {
        AuthUserProfileResult profile = new AuthUserProfileResult(
                2L, 10L, "研发部", "zhangsan", "张三", "/profile/avatar.png",
                "$2a$10$sensitiveHash", "0", "0", false,
                Set.of(3L), Set.of("common"), Set.of("system:user:list"), new Date());

        AuthUserProfileVO vo = AuthAdminConverter.toAuthUserProfileVO(profile);
        String json = new ObjectMapper().writeValueAsString(vo);

        assertNotNull(vo);
        assertEquals("/profile/avatar.png", vo.getAvatar());
        assertTrue(json.contains("\"userName\":\"zhangsan\""));
        assertFalse(json.contains("sensitiveHash"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("permissions"));
        assertFalse(json.contains("roleIds"));
    }
}
