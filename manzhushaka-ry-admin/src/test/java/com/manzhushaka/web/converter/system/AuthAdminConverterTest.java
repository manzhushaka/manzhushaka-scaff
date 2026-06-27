package com.manzhushaka.web.converter.system;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.manzhushaka.framework.web.command.LoginCommand;
import com.manzhushaka.framework.web.command.RegisterCommand;
import com.manzhushaka.web.dto.system.LoginRequest;
import com.manzhushaka.web.dto.system.RegisterRequest;

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
}