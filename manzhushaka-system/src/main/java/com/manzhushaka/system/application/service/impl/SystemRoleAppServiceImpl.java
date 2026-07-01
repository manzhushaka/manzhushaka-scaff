package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.application.command.CancelAuthUserCommand;
import com.manzhushaka.system.application.command.ChangeRoleStatusCommand;
import com.manzhushaka.system.application.command.CreateRoleCommand;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.application.command.UpdateRoleCommand;
import com.manzhushaka.system.application.query.RoleListQuery;
import com.manzhushaka.system.application.service.SystemRoleAppService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUserRole;
import com.manzhushaka.system.domain.repository.RoleRepository;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 系统角色应用服务实现
 *
 * @author manzhushaka
 */
@Service
public class SystemRoleAppServiceImpl implements SystemRoleAppService
{
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    @Override
    public List<SysRole> listRoles(RoleListQuery query)
    {
        SysRole role = new SysRole();
        role.setRoleName(query.roleName());
        role.setRoleKey(query.roleKey());
        role.setStatus(query.status());
        if (query.beginTime() != null)
        {
            role.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            role.getParams().put("endTime", query.endTime());
        }
        return roleRepository.selectRoleList(role);
    }

    @Override
    public SysRole getRoleDetail(Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        return roleRepository.selectRoleById(roleId);
    }

    @Override
    @Transactional
    public Long createRole(CreateRoleCommand command)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setRoleName(command.roleName());
        role.setRoleKey(command.roleKey());
        role.setRoleSort(command.roleSort());
        role.setDataScope(command.dataScope());
        role.setStatus(command.status());
        role.setMenuIds(command.menuIds());
        role.setDeptIds(command.deptIds());
        role.setRemark(command.remark());

        if (!roleService.checkRoleNameUnique(role))
        {
            throw new RuntimeException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role))
        {
            throw new RuntimeException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        roleRepository.insertRole(role);
        return role.getRoleId();
    }

    @Override
    @Transactional
    public void updateRole(UpdateRoleCommand command)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setRoleName(command.roleName());
        role.setRoleKey(command.roleKey());
        role.setRoleSort(command.roleSort());
        role.setDataScope(command.dataScope());
        role.setStatus(command.status());
        role.setMenuIds(command.menuIds());
        role.setDeptIds(command.deptIds());
        role.setRemark(command.remark());

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());

        if (!roleService.checkRoleNameUnique(role))
        {
            throw new RuntimeException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role))
        {
            throw new RuntimeException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        roleRepository.updateRole(role);
    }

    @Override
    @Transactional
    public void updateDataScope(DataScopeCommand command)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setDataScope(command.dataScope());
        role.setDeptIds(command.deptIds());

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleRepository.authDataScope(role);
    }

    @Override
    @Transactional
    public void changeStatus(ChangeRoleStatusCommand command)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setStatus(command.status());

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleRepository.updateRoleStatus(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long[] roleIds)
    {
        roleRepository.deleteRoleByIds(roleIds);
    }

    @Override
    public List<SysRole> selectRoleAll()
    {
        return roleRepository.selectRoleAll();
    }

    @Override
    public List<SysUser> allocatedUserList(SysUser user, Long roleId)
    {
        user.setRoleId(roleId);
        return userService.selectAllocatedList(user);
    }

    @Override
    public List<SysUser> unallocatedUserList(SysUser user, Long roleId)
    {
        user.setRoleId(roleId);
        return userService.selectUnallocatedList(user);
    }

    @Override
    @Transactional
    public void cancelAuthUser(CancelAuthUserCommand command)
    {
        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(command.roleId());
        // userIds contains a single user ID for single cancel
        if (command.userIds() != null && command.userIds().length > 0)
        {
            userRole.setUserId(command.userIds()[0]);
        }
        roleRepository.deleteAuthUser(userRole);
    }

    @Override
    @Transactional
    public void cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        roleRepository.deleteAuthUsers(roleId, userIds);
    }

    @Override
    @Transactional
    public void selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        roleRepository.insertAuthUsers(roleId, userIds);
    }
}