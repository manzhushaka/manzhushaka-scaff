package com.manzhushaka.web.converter.system;

import com.manzhushaka.framework.web.command.LoginCommand;
import com.manzhushaka.framework.web.command.RegisterCommand;
import com.manzhushaka.web.dto.system.LoginRequest;
import com.manzhushaka.web.dto.system.RegisterRequest;

/**
 * 认证管理转换器
 *
 * @author manzhushaka
 */
public class AuthAdminConverter
{
    /**
     * LoginRequest -> LoginCommand
     */
    public static LoginCommand toLoginCommand(LoginRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new LoginCommand(request.getUsername(), request.getPassword(),
                request.getCode(), request.getUuid());
    }

    /**
     * RegisterRequest -> RegisterCommand
     */
    public static RegisterCommand toRegisterCommand(RegisterRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new RegisterCommand(request.getUsername(), request.getPassword(),
                request.getCode(), request.getUuid());
    }
}