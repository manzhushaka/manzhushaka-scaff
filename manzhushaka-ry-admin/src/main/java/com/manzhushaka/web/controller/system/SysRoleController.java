package com.manzhushaka.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.web.service.SysPermissionService;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.application.service.SystemRoleAppService;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;
import com.manzhushaka.web.converter.system.role.RoleAdminConverter;
import com.manzhushaka.web.dto.system.role.CancelAuthUserRequest;
import com.manzhushaka.web.dto.system.role.ChangeRoleStatusRequest;
import com.manzhushaka.web.dto.system.role.CreateRoleRequest;
import com.manzhushaka.web.dto.system.role.DataScopeRequest;
import com.manzhushaka.web.dto.system.role.RoleListRequest;
import com.manzhushaka.web.dto.system.role.UpdateRoleRequest;

/**
 * 角色信息
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController
{
    @Autowired
    private SystemRoleAppService roleAppService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public TableDataInfo list(RoleListRequest request)
    {
        startPage();
        var query = RoleAdminConverter.toRoleListQuery(request);
        List<SysRole> list = roleAppService.listRoles(query);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:role:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role)
    {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId)
    {
        SysRole role = roleAppService.getRoleDetail(roleId);
        return success(role);
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateRoleRequest request)
    {
        SysRole checkRole = new SysRole();
        checkRole.setRoleName(request.getRoleName());
        checkRole.setRoleKey(request.getRoleKey());
        if (!roleService.checkRoleNameUnique(checkRole))
        {
            return error("新增角色'" + request.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(checkRole))
        {
            return error("新增角色'" + request.getRoleName() + "'失败，角色权限已存在");
        }
        var command = RoleAdminConverter.toCreateRoleCommand(request);
        roleAppService.createRole(command);
        return toAjax(true);
    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateRoleRequest request)
    {
        SysRole checkRole = new SysRole();
        checkRole.setRoleId(request.getRoleId());
        checkRole.setRoleName(request.getRoleName());
        checkRole.setRoleKey(request.getRoleKey());
        roleService.checkRoleAllowed(checkRole);
        roleService.checkRoleDataScope(request.getRoleId());
        if (!roleService.checkRoleNameUnique(checkRole))
        {
            return error("修改角色'" + request.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(checkRole))
        {
            return error("修改角色'" + request.getRoleName() + "'失败，角色权限已存在");
        }
        var command = RoleAdminConverter.toUpdateRoleCommand(request);
        roleAppService.updateRole(command);

        // 刷新所有持有该角色的在线用户权限
        tokenService.refreshPermissionByRoleId(request.getRoleId(), permissionService);
        return success();
    }

    /**
     * 修改保存数据权限
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody DataScopeRequest request)
    {
        SysRole checkRole = new SysRole();
        checkRole.setRoleId(request.getRoleId());
        roleService.checkRoleAllowed(checkRole);
        roleService.checkRoleDataScope(request.getRoleId());
        var command = RoleAdminConverter.toDataScopeCommand(request);
        roleAppService.updateDataScope(command);
        return toAjax(true);
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody ChangeRoleStatusRequest request)
    {
        SysRole checkRole = new SysRole();
        checkRole.setRoleId(request.getRoleId());
        roleService.checkRoleAllowed(checkRole);
        roleService.checkRoleDataScope(request.getRoleId());
        var command = RoleAdminConverter.toChangeRoleStatusCommand(request);
        roleAppService.changeStatus(command);
        return toAjax(true);
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds)
    {
        roleAppService.deleteRole(roleIds);
        return toAjax(true);
    }

    /**
     * 获取角色选择框列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(roleAppService.selectRoleAll());
    }

    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = roleAppService.allocatedUserList(user, user.getRoleId());
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = roleAppService.unallocatedUserList(user, user.getRoleId());
        return getDataTable(list);
    }

    /**
     * 取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody CancelAuthUserRequest request)
    {
        var command = RoleAdminConverter.toCancelAuthUserCommand(request);
        roleAppService.cancelAuthUser(command);
        return toAjax(true);
    }

    /**
     * 批量取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        roleAppService.cancelAuthUserAll(roleId, userIds);
        return toAjax(true);
    }

    /**
     * 批量选择用户授权
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleAppService.selectAuthUserAll(roleId, userIds);
        return toAjax(true);
    }

    /**
     * 获取对应角色部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/deptTree/{roleId}")
    public AjaxResult deptTree(@PathVariable("roleId") Long roleId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.selectDeptTreeList(new SysDept()));
        return ajax;
    }
}
