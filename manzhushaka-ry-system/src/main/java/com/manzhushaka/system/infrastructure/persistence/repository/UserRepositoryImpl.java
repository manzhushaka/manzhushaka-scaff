package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.UserRepository;
import com.manzhushaka.system.domain.SysUserRole;
import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.infrastructure.persistence.support.SysUserSensitiveFieldSupport;
import com.manzhushaka.system.mapper.SysUserMapper;
import com.manzhushaka.system.mapper.SysUserRoleMapper;

/**
 * 用户仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class UserRepositoryImpl implements UserRepository
{
    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysUser> selectUserList(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        return userMapper.selectUserList(user);
    }

    @Override
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        return userMapper.selectAllocatedList(user);
    }

    @Override
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        return userMapper.selectUnallocatedList(user);
    }

    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return userMapper.selectUserByUserName(userName);
    }

    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    @Override
    public int insertUser(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        return userMapper.insertUser(user);
    }

    @Override
    public int updateUser(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        return userMapper.updateUser(user);
    }

    @Override
    public int updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar);
    }

    @Override
    public int updateUserStatus(Long userId, String status)
    {
        return userMapper.updateUserStatus(userId, status);
    }

    @Override
    public int updateLoginInfo(Long userId, String loginIp, Date loginDate)
    {
        return userMapper.updateLoginInfo(userId, loginIp, loginDate);
    }

    @Override
    public int resetUserPwd(Long userId, String password)
    {
        return userMapper.resetUserPwd(userId, password);
    }

    @Override
    public int deleteUserById(Long userId)
    {
        return userMapper.deleteUserById(userId);
    }

    @Override
    public int deleteUserByIds(Long[] userIds)
    {
        return userMapper.deleteUserByIds(userIds);
    }

    @Override
    public SysUser checkUserNameUnique(String userName)
    {
        return userMapper.checkUserNameUnique(userName);
    }

    @Override
    public SysUser checkPhoneUnique(String phonenumber)
    {
        return userMapper.checkPhoneUnique(SensitiveFieldCryptoHolder.hash(phonenumber));
    }

    @Override
    public SysUser checkEmailUnique(String email)
    {
        return userMapper.checkEmailUnique(SensitiveFieldCryptoHolder.hash(email));
    }

    @Override
    public void checkUserAllowed(SysUser user)
    {
        // 业务校验：不允许操作超级管理员用户
        if (user != null && user.isAdmin())
        {
            throw new RuntimeException("不允许操作超级管理员用户");
        }
    }

    @Override
    public void checkUserDataScope(Long userId)
    {
        // 数据权限校验由上层业务控制，此处不做实现
    }

    @Override
    public void resetPwd(SysUser user)
    {
        userMapper.resetUserPwd(user.getUserId(), user.getPassword());
    }

    @Override
    public void updateUserStatus(SysUser user)
    {
        userMapper.updateUserStatus(user.getUserId(), user.getStatus());
    }

    @Override
    public int insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        if (roleIds == null || roleIds.length == 0)
        {
            return 1;
        }
        List<SysUserRole> userRoleList = new ArrayList<>(roleIds.length);
        for (Long roleId : roleIds)
        {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleList.add(userRole);
        }
        return userRoleMapper.batchUserRole(userRoleList);
    }

    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        SysUser sysUser = userMapper.checkUserNameUnique(user.getUserName());
        return sysUser == null || sysUser.getUserId().equals(user.getUserId());
    }

    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        String phonenumberHash = user.getPhonenumberHash();
        SysUser sysUser = userMapper.checkPhoneUnique(phonenumberHash);
        return sysUser == null || sysUser.getUserId().equals(user.getUserId());
    }

    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        SysUserSensitiveFieldSupport.fillHashes(user);
        String emailHash = user.getEmailHash();
        SysUser sysUser = userMapper.checkEmailUnique(emailHash);
        return sysUser == null || sysUser.getUserId().equals(user.getUserId());
    }

    @Override
    public String importUser(List<SysUser> userList, boolean updateSupport, String operName)
    {
        // 导入逻辑较复杂，实际由上层 service 处理，此处返回空
        return null;
    }
}
