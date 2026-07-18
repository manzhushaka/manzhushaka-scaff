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
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.web.service.SysPermissionService;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.application.service.SystemRoleAppService;
import com.manzhushaka.system.application.service.SystemDeptAppService;
import com.manzhushaka.system.application.result.system.RoleResult;
import com.manzhushaka.system.application.result.system.RoleExcelRow;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.web.converter.system.role.RoleAdminConverter;
import com.manzhushaka.web.dto.system.role.CancelAuthUserRequest;
import com.manzhushaka.web.dto.system.role.ChangeRoleStatusRequest;
import com.manzhushaka.web.dto.system.role.CreateRoleRequest;
import com.manzhushaka.web.dto.system.role.DataScopeRequest;
import com.manzhushaka.web.dto.system.role.RoleListRequest;
import com.manzhushaka.web.dto.system.role.RoleUserListRequest;
import com.manzhushaka.web.converter.system.shared.TreeSelectAdminConverter;
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
    private SystemDeptAppService deptAppService;

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
        List<RoleResult> list = roleAppService.listRoleResults(query);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:role:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, RoleListRequest request)
    {
        List<RoleExcelRow> list = roleAppService.listRoleExcelRows(RoleAdminConverter.toRoleListQuery(request));
        ExcelUtil<RoleExcelRow> util = new ExcelUtil<RoleExcelRow>(RoleExcelRow.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId)
    {
        RoleResult role = roleAppService.getRoleResult(roleId);
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
        var command = RoleAdminConverter.toCreateRoleCommand(request);
        roleAppService.createRole(command, SecurityContextHelper.getUsername());
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
        var command = RoleAdminConverter.toUpdateRoleCommand(request);
        roleAppService.updateRole(command, SecurityContextHelper.getUsername());

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
        var command = RoleAdminConverter.toDataScopeCommand(request);
        roleAppService.updateDataScope(command, SecurityContextHelper.getUsername());
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
        var command = RoleAdminConverter.toChangeRoleStatusCommand(request);
        roleAppService.changeStatus(command, SecurityContextHelper.getUsername());
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
        return success(roleAppService.selectRoleResults());
    }

    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(RoleUserListRequest request)
    {
        startPage();
        List<UserResult> list = roleAppService.allocatedUserResults(request.getUserName(),
                request.getPhonenumber(), request.getRoleId());
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(RoleUserListRequest request)
    {
        startPage();
        List<UserResult> list = roleAppService.unallocatedUserResults(request.getUserName(),
                request.getPhonenumber(), request.getRoleId());
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
        ajax.put("checkedKeys", deptAppService.listCheckedDeptIds(roleId));
        ajax.put("depts", TreeSelectAdminConverter.toVoList(
                deptAppService.listDeptTree(new com.manzhushaka.system.application.query.DeptQuery(
                        null, null, null, null, null))));
        return ajax;
    }
}
