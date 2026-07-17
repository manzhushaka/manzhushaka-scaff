package com.manzhushaka.web.converter.system.user;

import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.web.dto.system.user.ChangeUserStatusRequest;
import com.manzhushaka.web.dto.system.user.CreateUserRequest;
import com.manzhushaka.web.dto.system.user.ResetPwdRequest;
import com.manzhushaka.web.dto.system.user.UpdateUserRequest;
import com.manzhushaka.web.dto.system.user.UserListRequest;

/**
 * 用户管理转换器
 *
 * @author manzhushaka
 */
public class UserAdminConverter
{
    /**
     * UserListRequest -> UserListQuery
     */
    public static UserListQuery toUserListQuery(UserListRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new UserListQuery(
                request.getPageNum(),
                request.getPageSize(),
                request.getUserName(),
                request.getPhonenumber(),
                request.getStatus(),
                request.getDeptId(),
                request.getBeginTime(),
                request.getEndTime());
    }

    /**
     * CreateUserRequest -> CreateUserCommand
     */
    public static CreateUserCommand toCreateUserCommand(CreateUserRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new CreateUserCommand(
                request.getUserId(),
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getPhonenumber(),
                request.getEmail(),
                request.getSex(),
                request.getAvatar(),
                request.getStatus(),
                request.getDeptId(),
                request.getRoleIds());
    }

    /**
     * UpdateUserRequest -> UpdateUserCommand
     */
    public static UpdateUserCommand toUpdateUserCommand(UpdateUserRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new UpdateUserCommand(
                request.getUserId(),
                request.getUsername(),
                request.getNickname(),
                request.getPhonenumber(),
                request.getEmail(),
                request.getSex(),
                request.getStatus(),
                request.getDeptId(),
                request.getRoleIds());
    }

    /**
     * ResetPwdRequest -> ResetPwdCommand
     */
    public static ResetPwdCommand toResetPwdCommand(ResetPwdRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new ResetPwdCommand(request.getUserId(), request.getPassword());
    }

    /**
     * ChangeUserStatusRequest -> ChangeUserStatusCommand
     */
    public static ChangeUserStatusCommand toChangeUserStatusCommand(ChangeUserStatusRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new ChangeUserStatusCommand(request.getUserId(), request.getStatus());
    }

}
