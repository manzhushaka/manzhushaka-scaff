package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.SaveDeptCommand;
import com.manzhushaka.system.application.query.DeptQuery;
import com.manzhushaka.system.application.result.shared.TreeNodeResult;
import com.manzhushaka.system.application.result.system.DeptResult;

/**
 * 系统部门应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemDeptAppService
{
    /**
     * 查询部门列表。
     *
     * @param query 查询条件
     * @return 部门列表
     */
    List<DeptResult> listDeptResults(DeptQuery query);

    /**
     * 查询排除指定部门及其子部门后的列表。
     *
     * @param deptId 部门 ID
     * @return 部门列表
     */
    List<DeptResult> listDeptResultsExcluding(Long deptId);

    /**
     * 查询部门树。
     *
     * @param query 查询条件
     * @return 部门树节点
     */
    List<TreeNodeResult> listDeptTree(DeptQuery query);

    /**
     * 查询部门详情。
     *
     * @param deptId 部门 ID
     * @return 部门详情
     */
    DeptResult getDeptResult(Long deptId);

    /**
     * 查询角色已选部门 ID。
     *
     * @param roleId 角色 ID
     * @return 部门 ID 列表
     */
    List<Long> listCheckedDeptIds(Long roleId);

    /**
     * 新增部门。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createDept(SaveDeptCommand command, String operatorUsername);

    /**
     * 修改部门。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateDept(SaveDeptCommand command, String operatorUsername);

    /**
     * 修改部门排序。
     *
     * @param deptIds 部门 ID 数组
     * @param orderNums 排序值数组
     */
    void updateDeptSort(String[] deptIds, String[] orderNums);

    /**
     * 删除部门。
     *
     * @param deptId 部门 ID
     * @return 影响行数
     */
    int deleteDept(Long deptId);
}
