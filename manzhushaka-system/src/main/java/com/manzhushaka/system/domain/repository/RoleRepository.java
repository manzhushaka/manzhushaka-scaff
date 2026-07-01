package com.manzhushaka.system.domain.repository;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUserRole;

/**
 * 角色仓储接口
 *
 * @author manzhushaka
 */
public interface RoleRepository
{
    /**
     * 根据条件分页查询角色数据
     */
    List<SysRole> selectRoleList(SysRole role);

    /**
     * 根据用户ID查询角色
     */
    List<SysRole> selectRolePermissionByUserId(Long userId);

    /**
     * 查询所有角色
     */
    List<SysRole> selectRoleAll();

    /**
     * 根据用户ID获取角色选择框列表
     */
    List<Long> selectRoleListByUserId(Long userId);

    /**
     * 通过角色ID查询角色
     */
    SysRole selectRoleById(Long roleId);

    /**
     * 根据用户ID查询角色
     */
    List<SysRole> selectRolesByUserName(String userName);

    /**
     * 校验角色名称是否唯一
     */
    SysRole checkRoleNameUnique(String roleName);

    /**
     * 校验角色权限是否唯一
     */
    SysRole checkRoleKeyUnique(String roleKey);

    /**
     * 修改角色信息
     */
    int updateRole(SysRole role);

    /**
     * 新增角色信息
     */
    int insertRole(SysRole role);

    /**
     * 通过角色ID删除角色
     */
    int deleteRoleById(Long roleId);

    /**
     * 批量删除角色信息
     */
    int deleteRoleByIds(Long[] roleIds);

    /**
     * 检查角色是否允许操作
     */
    void checkRoleAllowed(SysRole role);

    /**
     * 检查角色数据权限
     */
    void checkRoleDataScope(Long roleId);

    /**
     * 检查角色数据权限（批量）
     */
    void checkRoleDataScope(Long[] roleIds);

    /**
     * 校验角色名称是否唯一（含排除自身）
     */
    boolean checkRoleNameUnique(SysRole role);

    /**
     * 校验角色权限是否唯一（含排除自身）
     */
    boolean checkRoleKeyUnique(SysRole role);

    /**
     * 更新角色状态
     */
    void updateRoleStatus(SysRole role);

    /**
     * 更新角色数据权限
     */
    void authDataScope(SysRole role);

    /**
     * 删除角色用户关联
     */
    int deleteAuthUser(SysUserRole userRole);

    /**
     * 批量删除角色用户关联
     */
    int deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     * 批量新增角色用户关联
     */
    int insertAuthUsers(Long roleId, Long[] userIds);
}