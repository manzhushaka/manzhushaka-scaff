package com.manzhushaka.web.converter.miniapp;

import com.manzhushaka.iip.application.member.command.MiniappLoginCommand;
import com.manzhushaka.web.dto.miniapp.LoginRequest;

/**
 * 小程序认证 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class MiniappAuthConverter
{
    private MiniappAuthConverter()
    {
    }

    public static MiniappLoginCommand toCommand(LoginRequest request)
    {
        return new MiniappLoginCommand(request.getPlatform(), request.getCode(), request.getNickname(),
                request.getAvatar());
    }
}
