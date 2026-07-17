package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.query.UserListQuery;

/**
 * 系统用户应用服务
 *
 * <p>负责编排用户相关的用例逻辑，协调多个领域模型的交互。
 * 作为事务边界和用例入口，不包含业务规则。</p>
 */
public interface SystemUserAppService
{
    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    List<SysUser> listUsers(UserListQuery query);

    /**
     * 导出用户列表。
     *
     * @param user 查询条件
     * @return 用户列表
     */
    List<SysUser> exportUsers(SysUser user);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getUserDetail(Long userId);

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @param operatorUsername 操作人账号
     * @return 新用户ID
     */
    Long createUser(CreateUserCommand command, String operatorUsername);

    /**
     * 修改用户
     *
     * @param command 修改命令
     * @param operatorUsername 操作人账号
     */
    void updateUser(UpdateUserCommand command, String operatorUsername);

    /**
     * 删除用户
     *
     * @param userIds 用户ID数组
     */
    void deleteUser(Long[] userIds);

    /**
     * 重置密码
     *
     * @param command 重置密码命令
     */
    void resetPwd(ResetPwdCommand command);

    /**
     * 修改状态
     *
     * @param command 修改状态命令
     */
    void changeStatus(ChangeUserStatusCommand command);

    /**
     * 授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID数组
     */
    void authRole(Long userId, Long[] roleIds);

    /**
     * 导入用户数据
     *
     * @param userList       用户数据列表
     * @param updateSupport  是否更新支持
     * @param operName       操作用户
     * @return 结果消息
     */
    String importUser(List<SysUser> userList, boolean updateSupport, String operName);
}
