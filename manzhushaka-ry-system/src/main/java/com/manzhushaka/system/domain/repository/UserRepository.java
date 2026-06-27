package com.manzhushaka.system.domain.repository;

import java.util.Date;
import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;

/**
 * 用户仓储接口
 *
 * @author manzhushaka
 */
public interface UserRepository
{
    /**
     * 根据条件分页查询用户列表
     */
    List<SysUser> selectUserList(SysUser user);

    /**
     * 根据条件分页查询已配用户角色列表
     */
    List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根据条件分页查询未分配用户角色列表
     */
    List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通过用户名查询用户
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户
     */
    SysUser selectUserById(Long userId);

    /**
     * 新增用户信息
     */
    int insertUser(SysUser user);

    /**
     * 修改用户信息
     */
    int updateUser(SysUser user);

    /**
     * 修改用户头像
     */
    int updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户状态
     */
    int updateUserStatus(Long userId, String status);

    /**
     * 更新用户登录信息
     */
    int updateLoginInfo(Long userId, String loginIp, Date loginDate);

    /**
     * 重置用户密码
     */
    int resetUserPwd(Long userId, String password);

    /**
     * 通过用户ID删除用户
     */
    int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     */
    int deleteUserByIds(Long[] userIds);

    /**
     * 校验用户名称是否唯一
     */
    SysUser checkUserNameUnique(String userName);

    /**
     * 校验手机号码是否唯一
     */
    SysUser checkPhoneUnique(String phonenumber);

    /**
     * 校验email是否唯一
     */
    SysUser checkEmailUnique(String email);

    /**
     * 检查用户是否允许操作
     */
    void checkUserAllowed(SysUser user);

    /**
     * 检查用户数据权限
     */
    void checkUserDataScope(Long userId);

    /**
     * 重置用户密码（通过用户对象）
     */
    void resetPwd(SysUser user);

    /**
     * 更新用户状态（通过用户对象）
     */
    void updateUserStatus(SysUser user);

    /**
     * 插入用户与角色的关联
     */
    int insertUserAuth(Long userId, Long[] roleIds);

    /**
     * 检查用户名是否唯一（含排除自身）
     */
    boolean checkUserNameUnique(SysUser user);

    /**
     * 检查手机号是否唯一（含排除自身）
     */
    boolean checkPhoneUnique(SysUser user);

    /**
     * 检查邮箱是否唯一（含排除自身）
     */
    boolean checkEmailUnique(SysUser user);

    /**
     * 导入用户
     */
    String importUser(List<SysUser> userList, boolean updateSupport, String operName);
}