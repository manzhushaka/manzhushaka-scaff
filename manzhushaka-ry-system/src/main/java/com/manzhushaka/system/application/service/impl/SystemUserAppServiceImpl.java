package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.utils.SecurityUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.system.domain.repository.UserRepository;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysPostService;
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
    private UserRepository userRepository;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

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
        return userRepository.selectUserList(user);
    }

    @Override
    public SysUser getUserDetail(Long userId)
    {
        userService.checkUserDataScope(userId);
        return userRepository.selectUserById(userId);
    }

    @Override
    public List<Long> getPostIdsByUserId(Long userId)
    {
        return postService.selectPostListByUserId(userId);
    }

    @Override
    @Transactional
    public Long createUser(CreateUserCommand command)
    {
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
        user.setPostIds(command.postIds());
        user.setPassword(SecurityUtils.encryptPassword(command.password()));
        user.setCreateBy(command.username());

        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new RuntimeException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new RuntimeException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        userRepository.insertUser(user);
        return user.getUserId();
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserCommand command)
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
        user.setPostIds(command.postIds());

        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());

        if (!userService.checkUserNameUnique(user))
        {
            throw new RuntimeException("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new RuntimeException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new RuntimeException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }

        userRepository.updateUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long[] userIds)
    {
        userRepository.deleteUserByIds(userIds);
    }

    @Override
    @Transactional
    public void resetPwd(ResetPwdCommand command)
    {
        SysUser user = new SysUser();
        user.setUserId(command.userId());
        user.setPassword(SecurityUtils.encryptPassword(command.password()));
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        userRepository.resetUserPwd(user.getUserId(), user.getPassword());
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
        userRepository.updateUserStatus(user.getUserId(), user.getStatus());
    }

    @Override
    @Transactional
    public void authRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userRepository.insertUserAuth(userId, roleIds);
    }

    @Override
    @Transactional
    public String importUser(List<SysUser> userList, boolean updateSupport, String operName)
    {
        return userService.importUser(userList, updateSupport, operName);
    }
}