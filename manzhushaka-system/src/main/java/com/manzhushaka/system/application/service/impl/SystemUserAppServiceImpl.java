package com.manzhushaka.system.application.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.security.PasswordStrengthUtils;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.command.UpdateProfileCommand;
import com.manzhushaka.system.application.command.UpdateOwnPasswordCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.system.application.result.system.SystemResultMapper;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.system.application.result.system.UserExcelRow;
import com.manzhushaka.system.application.result.system.UserExportCursorRow;
import com.manzhushaka.system.application.result.system.UserImportBatchResult;
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

    private List<SysUser> listUserEntities(UserListQuery query)
    {
        return userService.selectUserList(toUserEntity(query));
    }

    private SysUser toUserEntity(UserListQuery query)
    {
        SysUser user = new SysUser();
        user.setUserName(query.userName());
        if (StringUtils.isNotEmpty(query.phonenumber()))
        {
            user.setPhonenumberHash(SensitiveFieldCryptoHolder.hash(query.phonenumber()));
        }
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
        return user;
    }

    /**
     * 分页查询用户结果。
     *
     * @param query 查询条件
     * @return 用户结果列表
     */
    @Override
    public List<UserResult> listUserResults(UserListQuery query)
    {
        return SystemResultMapper.toUserResults(listUserEntities(query));
    }

    /**
     * 导出用户结果。
     *
     * @param query 查询条件
     * @return 用户结果列表
     */
    @Override
    public List<UserExcelRow> exportUserResults(UserListQuery query)
    {
        return SystemResultMapper.toUserExcelRows(listUserEntities(query));
    }

    /**
     * 导入用户行。
     *
     * @param rows 用户行列表
     * @param updateSupport 是否更新已有用户
     * @param operName 操作人
     * @return 导入结果消息
     */
    @Override
    public String importUserRows(List<UserExcelRow> rows, boolean updateSupport, String operName)
    {
        List<SysUser> users = rows.stream().map(row -> {
            SysUser user = new SysUser();
            user.setUserId(row.getUserId());
            user.setDeptId(row.getDeptId());
            user.setUserName(row.getUserName());
            user.setNickName(row.getNickName());
            user.setEmail(row.getEmail());
            user.setPhonenumber(row.getPhonenumber());
            user.setSex(row.getSex());
            user.setStatus(row.getStatus());
            return user;
        }).toList();
        return userService.importUser(users, updateSupport, operName);
    }

    /**
     * 统计异步导出用户数量。
     *
     * @param query 查询条件
     * @return 用户数量
     */
    @Override
    public long countUserExportRows(UserListQuery query)
    {
        return userService.countUserForExport(toUserEntity(query));
    }

    /**
     * 按稳定游标读取用户导出批次。
     *
     * @param query 查询条件
     * @param cursorTime 游标创建时间
     * @param cursorId 游标用户 ID
     * @param limit 批次大小
     * @return 用户导出行
     */
    @Override
    public List<UserExportCursorRow> listUserExportRows(UserListQuery query, Date cursorTime,
            Long cursorId, int limit)
    {
        return userService.selectUserExportBatch(toUserEntity(query), cursorTime, cursorId, limit)
                .stream().map(user -> {
                    UserExportCursorRow row = new UserExportCursorRow();
                    row.setUserId(user.getUserId());
                    row.setDeptId(user.getDeptId());
                    row.setUserName(user.getUserName());
                    row.setNickName(user.getNickName());
                    row.setEmail(user.getEmail());
                    row.setPhonenumber(user.getPhonenumber());
                    row.setSex(user.getSex());
                    row.setStatus(user.getStatus());
                    row.setDeptName(user.getDept() == null ? null : user.getDept().getDeptName());
                    row.setDeptLeader(user.getDept() == null ? null : user.getDept().getLeader());
                    row.setLoginIp(user.getLoginIp());
                    row.setLoginDate(user.getLoginDate());
                    row.setCreateTime(user.getCreateTime());
                    return row;
                }).toList();
    }

    /**
     * 导入一批用户并返回成功失败统计。
     *
     * @param rows 用户行
     * @param updateSupport 是否更新已存在用户
     * @param operName 操作人
     * @return 导入批次结果
     */
    @Override
    public UserImportBatchResult importUserRowsBatch(List<UserExcelRow> rows, boolean updateSupport,
            String operName)
    {
        long success = 0L;
        long failure = 0L;
        StringBuilder errors = new StringBuilder();
        for (UserExcelRow row : rows)
        {
            try
            {
                importUserRows(List.of(row), updateSupport, operName);
                success++;
            }
            catch (Exception exception)
            {
                failure++;
                if (errors.length() < 1800)
                {
                    errors.append("账号 ").append(row.getUserName()).append(": ")
                            .append(exception.getMessage()).append(';');
                }
            }
        }
        return new UserImportBatchResult(success, failure, errors.toString());
    }

    @Override
    public String getUserRoleGroup(String username)
    {
        return userService.selectUserRoleGroup(username);
    }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileCommand command)
    {
        SysUser user = userService.selectUserById(command.userId());
        user.setNickName(command.nickName());
        user.setEmail(command.email());
        user.setPhonenumber(command.phonenumber());
        user.setSex(command.sex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("修改用户'" + command.username() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("修改用户'" + command.username() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(user) <= 0)
        {
            throw new ServiceException("修改个人信息异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public String updateOwnPassword(UpdateOwnPasswordCommand command)
    {
        SysUser user = userService.selectUserById(command.userId());
        String password = user.getPassword();
        if (!PasswordUtils.matches(command.oldPassword(), password))
        {
            throw new ServiceException("修改密码失败，旧密码错误");
        }
        if (PasswordUtils.matches(command.newPassword(), password))
        {
            throw new ServiceException("新密码不能与旧密码相同");
        }
        validateStrongPassword(command.username(), command.newPassword());
        String encryptedPassword = PasswordUtils.encrypt(command.newPassword());
        if (userService.resetUserPwd(command.userId(), encryptedPassword) <= 0)
        {
            throw new ServiceException("修改密码异常，请联系管理员");
        }
        return encryptedPassword;
    }

    @Override
    public boolean updateAvatar(Long userId, String avatar)
    {
        return userService.updateUserAvatar(userId, avatar);
    }

    /**
     * 获取用户结果。
     *
     * @param userId 用户 ID
     * @return 用户结果
     */
    @Override
    public UserResult getUserResult(Long userId)
    {
        userService.checkUserDataScope(userId);
        return SystemResultMapper.toUserResult(userService.selectUserById(userId));
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
