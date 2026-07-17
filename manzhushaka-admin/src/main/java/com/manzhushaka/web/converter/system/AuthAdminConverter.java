package com.manzhushaka.web.converter.system;

import com.manzhushaka.framework.web.command.LoginCommand;
import com.manzhushaka.framework.web.command.RegisterCommand;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.web.dto.system.LoginRequest;
import com.manzhushaka.web.dto.system.RegisterRequest;
import com.manzhushaka.web.vo.system.user.AuthUserProfileVO;

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

    /**
     * 将内部认证资料转换为前端当前用户视图。
     *
     * @param profile 内部认证资料
     * @return 当前用户视图，不存在时返回 {@code null}
     */
    public static AuthUserProfileVO toAuthUserProfileVO(AuthUserProfileResult profile)
    {
        if (profile == null)
        {
            return null;
        }
        AuthUserProfileVO vo = new AuthUserProfileVO();
        vo.setUserId(profile.userId());
        vo.setDeptId(profile.deptId());
        vo.setDeptName(profile.deptName());
        vo.setUserName(profile.username());
        vo.setNickName(profile.nickName());
        vo.setAvatar(profile.avatar());
        return vo;
    }
}
