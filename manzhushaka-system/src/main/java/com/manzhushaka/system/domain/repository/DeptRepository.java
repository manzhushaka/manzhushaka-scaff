package com.manzhushaka.system.domain.repository;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;

/**
 * 部门仓储接口
 *
 * @author manzhushaka
 */
public interface DeptRepository
{
    /**
     * 查询部门管理数据
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 根据角色ID查询部门树信息
     */
    List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly);

    /**
     * 根据部门ID查询信息
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门
     */
    List<SysDept> selectChildrenDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     */
    int selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在子节点
     */
    int hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     */
    int checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     */
    SysDept checkDeptNameUnique(String deptName, Long parentId);

    /**
     * 新增部门信息
     */
    int insertDept(SysDept dept);

    /**
     * 修改部门信息
     */
    int updateDept(SysDept dept);

    /**
     * 修改所在部门正常状态
     */
    void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 修改子元素关系
     */
    int updateDeptChildren(List<SysDept> depts);

    /**
     * 保存部门排序
     */
    void updateDeptSort(SysDept dept);

    /**
     * 删除部门管理信息
     */
    int deleteDeptById(Long deptId);

    /**
     * 检查部门数据权限
     */
    void checkDeptDataScope(Long deptId);
}