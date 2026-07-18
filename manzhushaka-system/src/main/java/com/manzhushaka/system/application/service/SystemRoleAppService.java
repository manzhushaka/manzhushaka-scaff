package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.CancelAuthUserCommand;
import com.manzhushaka.system.application.command.ChangeRoleStatusCommand;
import com.manzhushaka.system.application.command.CreateRoleCommand;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.application.command.UpdateRoleCommand;
import com.manzhushaka.system.application.query.RoleListQuery;
import com.manzhushaka.system.application.result.system.RoleResult;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.system.application.result.system.RoleExcelRow;

/**
 * 系统角色应用服务
 *
 * 负责编排角色相关的用例逻辑，协调多个领域模型的交互。
 * 作为事务边界和用例入口，不包含业务规则。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemRoleAppService
{
    /**
     * 查询角色结果列表。
     *
     * @param query 查询条件
     * @return 角色结果列表
     */
    List<RoleResult> listRoleResults(RoleListQuery query);

    /**
     * 查询角色导出行。
     *
     * @param query 查询条件
     * @return 角色导出行
     */
    List<RoleExcelRow> listRoleExcelRows(RoleListQuery query);

    /**
     * 获取角色结果。
     *
     * @param roleId 角色 ID
     * @return 角色结果
     */
    RoleResult getRoleResult(Long roleId);

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
     * 获取角色选项结果。
     *
     * @return 角色结果列表
     */
    List<RoleResult> selectRoleResults();

    /**
     * 获取用户角色授权结果。
     *
     * @param userId 用户 ID
     * @return 带选中状态的角色结果
     */
    List<RoleResult> selectRoleResultsByUserId(Long userId);

    /**
     * 查询已分配用户结果。
     *
     * @param userName 用户名
     * @param phonenumber 手机号
     * @param roleId 角色 ID
     * @return 用户结果列表
     */
    List<UserResult> allocatedUserResults(String userName, String phonenumber, Long roleId);

    /**
     * 查询未分配用户结果。
     *
     * @param userName 用户名
     * @param phonenumber 手机号
     * @param roleId 角色 ID
     * @return 用户结果列表
     */
    List<UserResult> unallocatedUserResults(String userName, String phonenumber, Long roleId);

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
