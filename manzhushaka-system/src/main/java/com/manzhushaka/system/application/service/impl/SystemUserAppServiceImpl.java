package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.security.PasswordStrengthUtils;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 系统用户应用服务实现
 *
 * @author manzhushaka
 */
@Service
public class SystemUserAppServiceImpl implements SystemUserAppService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Override
    public List<SysUser> listUsers(UserListQuery query)
    {
        SysUser user = new SysUser();
        user.setUserName(query.userName());
        user.setPhonenumber(query.phonenumber());
        user.setStatus(query.status());
        user.setDeptId(query.deptId());
        if (query.beginTime() != null)
        {
            user.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            user.getParams().put("endTime", query.endTime());
        }
        return userService.selectUserList(user);
    }

    @Override
    public List<SysUser> exportUsers(SysUser user)
    {
        return userService.selectUserList(user);
    }

    @Override
    public SysUser getUserDetail(Long userId)
    {
        userService.checkUserDataScope(userId);
        return userService.selectUserById(userId);
    }

    @Override
    @Transactional
    public Long createUser(CreateUserCommand command, String operatorUsername)
    {
        validateStrongPassword(command.username(), command.password());

        SysUser user = new SysUser();
        user.setUserId(command.userId());
        user.setUserName(command.username());
        user.setNickName(command.nickname());
        user.setPhonenumber(command.phonenumber());
        user.setEmail(command.email());
        user.setSex(command.sex());
        user.setAvatar(command.avatar());
        user.setStatus(command.status());
        user.setDeptId(command.deptId());
        user.setRoleIds(command.roleIds());
        user.setPassword(PasswordUtils.encrypt(command.password()));
        user.setCreateBy(operatorUsername);

        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());

        if (!userService.checkUserNameUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        userService.insertUser(user);
        return user.getUserId();
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserCommand command, String operatorUsername)
    {
        SysUser user = new SysUser();
        user.setUserId(command.userId());
        user.setUserName(command.username());
        user.setNickName(command.nickname());
        user.setPhonenumber(command.phonenumber());
        user.setEmail(command.email());
        user.setSex(command.sex());
        user.setStatus(command.status());
        user.setDeptId(command.deptId());
        user.setRoleIds(command.roleIds());
        user.setUpdateBy(operatorUsername);

        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());

        if (!userService.checkUserNameUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        userService.updateUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long[] userIds)
    {
        userService.deleteUserByIds(userIds);
    }

    @Override
    @Transactional
    public void resetPwd(ResetPwdCommand command)
    {
        validateStrongPassword(null, command.password());
        SysUser currentUser = userService.selectUserById(command.userId());
        validateStrongPassword(currentUser != null ? currentUser.getUserName() : null, command.password());

        SysUser user = new SysUser();
        user.setUserId(command.userId());
        user.setPassword(PasswordUtils.encrypt(command.password()));
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        userService.resetUserPwd(user.getUserId(), user.getPassword());
    }

    @Override
    @Transactional
    public void changeStatus(ChangeUserStatusCommand command)
    {
        SysUser user = new SysUser();
        user.setUserId(command.userId());
        user.setStatus(command.status());
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        userService.updateUserStatus(user);
    }

    @Override
    @Transactional
    public void authRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
    }

    @Override
    @Transactional
    public String importUser(List<SysUser> userList, boolean updateSupport, String operName)
    {
        return userService.importUser(userList, updateSupport, operName);
    }

    /**
     * 校验新密码强度。
     *
     * @param username 用户名
     * @param password 明文密码
     */
    private void validateStrongPassword(String username, String password)
    {
        String message = PasswordStrengthUtils.getWeakPasswordMessage(username, password);
        if (StringUtils.isNotEmpty(message))
        {
            throw new ServiceException(message);
        }
    }
}
