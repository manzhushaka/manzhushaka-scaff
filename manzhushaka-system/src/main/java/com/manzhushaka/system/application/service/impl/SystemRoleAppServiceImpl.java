package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.application.command.CancelAuthUserCommand;
import com.manzhushaka.system.application.command.ChangeRoleStatusCommand;
import com.manzhushaka.system.application.command.CreateRoleCommand;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.application.command.UpdateRoleCommand;
import com.manzhushaka.system.application.query.RoleListQuery;
import com.manzhushaka.system.application.service.SystemRoleAppService;
import com.manzhushaka.system.application.result.system.RoleResult;
import com.manzhushaka.system.application.result.system.SystemResultMapper;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.system.application.result.system.RoleExcelRow;
import com.manzhushaka.system.domain.SysUserRole;
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
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    private List<SysRole> listRoleEntities(RoleListQuery query)
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
        return roleService.selectRoleList(role);
    }

    /**
     * 查询角色结果列表。
     *
     * @param query 查询条件
     * @return 角色结果列表
     */
    @Override
    public List<RoleResult> listRoleResults(RoleListQuery query)
    {
        return SystemResultMapper.toRoleResults(listRoleEntities(query));
    }

    /**
     * 查询角色导出行。
     *
     * @param query 查询条件
     * @return 角色导出行
     */
    @Override
    public List<RoleExcelRow> listRoleExcelRows(RoleListQuery query)
    {
        return SystemResultMapper.toRoleExcelRows(listRoleEntities(query));
    }

    /**
     * 获取角色结果。
     *
     * @param roleId 角色 ID
     * @return 角色结果
     */
    @Override
    public RoleResult getRoleResult(Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        return SystemResultMapper.toRoleResult(roleService.selectRoleById(roleId));
    }

    @Override
    @Transactional
    public Long createRole(CreateRoleCommand command, String operatorUsername)
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
        role.setCreateBy(operatorUsername);

        if (!roleService.checkRoleNameUnique(role))
        {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role))
        {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        roleService.insertRole(role);
        return role.getRoleId();
    }

    @Override
    @Transactional
    public void updateRole(UpdateRoleCommand command, String operatorUsername)
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
        role.setUpdateBy(operatorUsername);

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());

        if (!roleService.checkRoleNameUnique(role))
        {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (!roleService.checkRoleKeyUnique(role))
        {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        roleService.updateRole(role);
    }

    @Override
    @Transactional
    public void updateDataScope(DataScopeCommand command, String operatorUsername)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setDataScope(command.dataScope());
        role.setDeptIds(command.deptIds());
        role.setUpdateBy(operatorUsername);

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleService.authDataScope(role);
    }

    @Override
    @Transactional
    public void changeStatus(ChangeRoleStatusCommand command, String operatorUsername)
    {
        SysRole role = new SysRole();
        role.setRoleId(command.roleId());
        role.setStatus(command.status());
        role.setUpdateBy(operatorUsername);

        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleService.updateRoleStatus(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long[] roleIds)
    {
        roleService.deleteRoleByIds(roleIds);
    }

    /**
     * 获取角色选项结果。
     *
     * @return 角色结果列表
     */
    @Override
    public List<RoleResult> selectRoleResults()
    {
        return SystemResultMapper.toRoleResults(roleService.selectRoleAll());
    }

    /**
     * 获取用户角色授权结果。
     *
     * @param userId 用户 ID
     * @return 带选中状态的角色结果
     */
    @Override
    public List<RoleResult> selectRoleResultsByUserId(Long userId)
    {
        return SystemResultMapper.toRoleResults(roleService.selectRolesByUserId(userId));
    }

    /**
     * 查询已分配用户结果。
     *
     * @param userName 用户名
     * @param phonenumber 手机号
     * @param roleId 角色 ID
     * @return 用户结果列表
     */
    @Override
    public List<UserResult> allocatedUserResults(String userName, String phonenumber, Long roleId)
    {
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setPhonenumber(phonenumber);
        user.setRoleId(roleId);
        return SystemResultMapper.toUserResults(userService.selectAllocatedList(user));
    }

    /**
     * 查询未分配用户结果。
     *
     * @param userName 用户名
     * @param phonenumber 手机号
     * @param roleId 角色 ID
     * @return 用户结果列表
     */
    @Override
    public List<UserResult> unallocatedUserResults(String userName, String phonenumber, Long roleId)
    {
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setPhonenumber(phonenumber);
        user.setRoleId(roleId);
        return SystemResultMapper.toUserResults(userService.selectUnallocatedList(user));
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
        roleService.deleteAuthUser(userRole);
    }

    @Override
    @Transactional
    public void cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.deleteAuthUsers(roleId, userIds);
    }

    @Override
    @Transactional
    public void selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        roleService.insertAuthUsers(roleId, userIds);
    }

}
