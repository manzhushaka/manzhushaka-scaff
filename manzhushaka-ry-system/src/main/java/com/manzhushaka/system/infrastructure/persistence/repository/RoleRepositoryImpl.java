package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.RoleRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUserRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysRoleMapper;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysUserRoleMapper;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysRoleMenuMapper;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysRoleDeptMapper;

/**
 * 角色仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    @Override
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    @Override
    public List<SysRole> selectRolePermissionByUserId(Long userId)
    {
        return roleMapper.selectRolePermissionByUserId(userId);
    }

    @Override
    public List<SysRole> selectRoleAll()
    {
        return roleMapper.selectRoleAll();
    }

    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    @Override
    public List<SysRole> selectRolesByUserName(String userName)
    {
        return roleMapper.selectRolesByUserName(userName);
    }

    @Override
    public SysRole checkRoleNameUnique(String roleName)
    {
        return roleMapper.checkRoleNameUnique(roleName);
    }

    @Override
    public SysRole checkRoleKeyUnique(String roleKey)
    {
        return roleMapper.checkRoleKeyUnique(roleKey);
    }

    @Override
    public int updateRole(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    @Override
    public int insertRole(SysRole role)
    {
        return roleMapper.insertRole(role);
    }

    @Override
    public int deleteRoleById(Long roleId)
    {
        return roleMapper.deleteRoleById(roleId);
    }

    @Override
    public int deleteRoleByIds(Long[] roleIds)
    {
        return roleMapper.deleteRoleByIds(roleIds);
    }

    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (role != null && role.isAdmin())
        {
            throw new RuntimeException("不允许操作超级管理员角色");
        }
    }

    @Override
    public void checkRoleDataScope(Long roleId)
    {
        // 数据权限校验由上层业务控制，此处不做实现
    }

    @Override
    public void checkRoleDataScope(Long[] roleIds)
    {
        // 数据权限校验由上层业务控制，此处不做实现
    }

    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        SysRole sysRole = roleMapper.checkRoleNameUnique(role.getRoleName());
        return sysRole == null || sysRole.getRoleId().equals(role.getRoleId());
    }

    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        SysRole sysRole = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        return sysRole == null || sysRole.getRoleId().equals(role.getRoleId());
    }

    @Override
    public void updateRoleStatus(SysRole role)
    {
        roleMapper.updateRole(role);
    }

    @Override
    public void authDataScope(SysRole role)
    {
        roleMapper.updateRole(role);
    }

    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        return 0;
    }
}