package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.application.command.CancelAuthUserCommand;
import com.manzhushaka.system.application.command.ChangeRoleStatusCommand;
import com.manzhushaka.system.application.command.CreateRoleCommand;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.application.command.UpdateRoleCommand;
import com.manzhushaka.system.application.query.RoleListQuery;

/**
 * 系统角色应用服务
 *
 * <p>负责编排角色相关的用例逻辑，协调多个领域模型的交互。
 * 作为事务边界和用例入口，不包含业务规则。</p>
 */
public interface SystemRoleAppService
{
    /**
     * 分页查询角色列表
     *
     * @param query 查询条件
     * @return 角色列表
     */
    List<SysRole> listRoles(RoleListQuery query);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    SysRole getRoleDetail(Long roleId);

    /**
     * 创建角色
     *
     * @param command 创建命令
     * @param operatorUsername 操作人账号
     * @return 新角色ID
     */
    Long createRole(CreateRoleCommand command, String operatorUsername);

    /**
     * 修改角色
     *
     * @param command 修改命令
     * @param operatorUsername 操作人账号
     */
    void updateRole(UpdateRoleCommand command, String operatorUsername);

    /**
     * 修改数据权限
     *
     * @param command 数据权限命令
     * @param operatorUsername 操作人账号
     */
    void updateDataScope(DataScopeCommand command, String operatorUsername);

    /**
     * 修改角色状态
     *
     * @param command 修改状态命令
     * @param operatorUsername 操作人账号
     */
    void changeStatus(ChangeRoleStatusCommand command, String operatorUsername);

    /**
     * 删除角色
     *
     * @param roleIds 角色ID数组
     */
    void deleteRole(Long[] roleIds);

    /**
     * 获取所有角色（角色选择框）
     *
     * @return 所有角色列表
     */
    List<SysRole> selectRoleAll();

    /**
     * 查询已分配用户角色列表
     *
     * @param user   用户查询条件
     * @param roleId 角色ID
     * @return 已分配用户列表
     */
    List<SysUser> allocatedUserList(SysUser user, Long roleId);

    /**
     * 查询未分配用户角色列表
     *
     * @param user   用户查询条件
     * @param roleId 角色ID
     * @return 未分配用户列表
     */
    List<SysUser> unallocatedUserList(SysUser user, Long roleId);

    /**
     * 取消授权用户
     *
     * @param command 取消授权命令
     */
    void cancelAuthUser(CancelAuthUserCommand command);

    /**
     * 批量取消授权用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     */
    void cancelAuthUserAll(Long roleId, Long[] userIds);

    /**
     * 批量选择用户授权
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     */
    void selectAuthUserAll(Long roleId, Long[] userIds);
}
