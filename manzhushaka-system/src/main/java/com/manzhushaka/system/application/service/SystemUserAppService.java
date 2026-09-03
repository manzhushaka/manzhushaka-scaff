package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.ChangeUserStatusCommand;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateUserCommand;
import com.manzhushaka.system.application.command.UpdateProfileCommand;
import com.manzhushaka.system.application.command.UpdateOwnPasswordCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.system.application.result.system.UserExcelRow;
import com.manzhushaka.system.application.result.system.UserExportCursorRow;
import com.manzhushaka.system.application.result.system.UserImportBatchResult;

/**
 * 系统用户应用服务
 *
 * 负责编排用户相关的用例逻辑，协调多个领域模型的交互。
 * 作为事务边界和用例入口，不包含业务规则。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemUserAppService
{
    /**
     * 分页查询用户结果。
     *
     * @param query 查询条件
     * @return 用户结果列表
     */
    List<UserResult> listUserResults(UserListQuery query);

    /**
     * 导出用户结果。
     *
     * @param query 查询条件
     * @return 用户结果列表
     */
    List<UserExcelRow> exportUserResults(UserListQuery query);

    /**
     * 导入用户行。
     *
     * @param rows 用户行列表
     * @param updateSupport 是否更新已有用户
     * @param operName 操作人
     * @return 导入结果消息
     */
    String importUserRows(List<UserExcelRow> rows, boolean updateSupport, String operName);

    /**
     * 统计异步导出用户数量。
     *
     * @param query 查询条件
     * @return 用户数量
     */
    long countUserExportRows(UserListQuery query);

    /**
     * 按创建时间和用户 ID 游标读取导出批次。
     *
     * @param query 查询条件
     * @param cursorTime 游标创建时间
     * @param cursorId 游标用户 ID
     * @param limit 批次大小
     * @return 用户导出行
     */
    List<UserExportCursorRow> listUserExportRows(UserListQuery query, java.util.Date cursorTime,
            Long cursorId, int limit);

    /**
     * 导入一批用户并返回结构化计数。
     *
     * @param rows 用户行
     * @param updateSupport 是否更新已存在用户
     * @param operName 操作人
     * @return 导入批次结果
     */
    UserImportBatchResult importUserRowsBatch(List<UserExcelRow> rows, boolean updateSupport, String operName);

    /**
     * 获取用户角色组文本。
     *
     * @param username 用户名
     * @return 角色组文本
     */
    String getUserRoleGroup(String username);

    /**
     * 更新当前用户资料。
     *
     * @param command 更新命令
     */
    void updateProfile(UpdateProfileCommand command);

    /**
     * 修改当前用户密码。
     *
     * @param command 修改密码命令
     * @return 加密后的新密码
     */
    String updateOwnPassword(UpdateOwnPasswordCommand command);

    /**
     * 更新用户头像。
     *
     * @param userId 用户 ID
     * @param avatar 头像地址
     * @return 是否更新成功
     */
    boolean updateAvatar(Long userId, String avatar);

    /**
     * 获取用户结果。
     *
     * @param userId 用户 ID
     * @return 用户结果
     */
    UserResult getUserResult(Long userId);

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

}
