package com.manzhushaka.web.converter.system.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.manzhushaka.common.core.domain.entity.SysRole;
import com.manzhushaka.common.core.domain.entity.SysUser;
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
import com.manzhushaka.web.vo.system.user.UserDetailVO;
import com.manzhushaka.web.vo.system.user.UserListVO;

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
                request.getRoleIds(),
                request.getPostIds());
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
                request.getRoleIds(),
                request.getPostIds());
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

    /**
     * SysUser -> UserListVO
     */
    public static UserListVO toUserListVO(SysUser user)
    {
        if (user == null)
        {
            return null;
        }
        UserListVO vo = new UserListVO();
        vo.setUserId(user.getUserId());
        vo.setDeptId(user.getDeptId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setEmail(user.getEmail());
        vo.setPhonenumber(user.getPhonenumber());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        if (user.getDept() != null)
        {
            vo.setDeptName(user.getDept().getDeptName());
        }
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * List<SysUser> -> List<UserListVO>
     */
    public static List<UserListVO> toUserListVO(List<SysUser> users)
    {
        if (users == null)
        {
            return new ArrayList<>();
        }
        return users.stream()
                .map(UserAdminConverter::toUserListVO)
                .collect(Collectors.toList());
    }

    /**
     * SysUser + 辅助数据 -> UserDetailVO
     */
    public static UserDetailVO toUserDetailVO(SysUser user, List<Long> postIds,
            List<SysRole> allRoles, List<Long> roleIds, List<?> allPosts)
    {
        if (user == null)
        {
            return null;
        }
        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(user.getUserId());
        vo.setDeptId(user.getDeptId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setEmail(user.getEmail());
        vo.setPhonenumber(user.getPhonenumber());
        vo.setSex(user.getSex());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        if (user.getDept() != null)
        {
            vo.setDeptName(user.getDept().getDeptName());
        }
        vo.setRoles(user.getRoles());
        vo.setRoleIds(roleIds);
        vo.setPostIds(postIds);
        vo.setAllRoles(allRoles);
        if (allPosts != null)
        {
            @SuppressWarnings("unchecked")
            List<Object> posts = (List<Object>) allPosts;
            vo.setAllPosts(posts);
        }
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}