package com.manzhushaka.web.controller.system;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.web.multipart.MultipartFile;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.system.application.service.SystemDeptAppService;
import com.manzhushaka.system.application.service.SystemRoleAppService;
import com.manzhushaka.system.application.result.system.RoleResult;
import com.manzhushaka.system.application.result.system.UserExcelRow;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.web.converter.system.user.UserAdminConverter;
import com.manzhushaka.web.converter.system.DeptAdminConverter;
import com.manzhushaka.web.dto.system.user.ChangeUserStatusRequest;
import com.manzhushaka.web.dto.system.user.CreateUserRequest;
import com.manzhushaka.web.dto.system.user.ResetPwdRequest;
import com.manzhushaka.web.dto.system.user.UpdateUserRequest;
import com.manzhushaka.web.converter.system.shared.TreeSelectAdminConverter;
import com.manzhushaka.web.dto.system.user.UserListRequest;
import com.manzhushaka.web.dto.system.SysDeptTreeRequest;

/**
 * 用户信息
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    @Autowired
    private SystemUserAppService userAppService;

    @Autowired
    private SystemRoleAppService roleAppService;

    @Autowired
    private SystemDeptAppService deptAppService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserListRequest request)
    {
        startPage();
        var query = UserAdminConverter.toUserListQuery(request);
        List<UserResult> list = userAppService.listUserResults(query);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserListRequest request)
    {
        List<UserExcelRow> list = userAppService.exportUserResults(
                UserAdminConverter.toUserListQuery(request));
        ExcelUtil<UserExcelRow> util = new ExcelUtil<UserExcelRow>(UserExcelRow.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<UserExcelRow> util = new ExcelUtil<UserExcelRow>(UserExcelRow.class);
        List<UserExcelRow> userList = util.importExcel(file.getInputStream());
        String operName = SecurityContextHelper.getUsername();
        String message = userAppService.importUserRows(userList, updateSupport, operName);
        return success(message);
    }

    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<UserExcelRow> util = new ExcelUtil<UserExcelRow>(UserExcelRow.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = { "/", "/{userId}" })
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(userId))
        {
            UserResult user = userAppService.getUserResult(userId);
            ajax.put(AjaxResult.DATA_TAG, user);
            ajax.put("roleIds", user.roleIds());
        }
        List<RoleResult> roles = roleAppService.selectRoleResults();
        ajax.put("roles", SecurityContextHelper.isAdmin(userId) ? roles
                : roles.stream().filter(role -> role.roleId() == null || role.roleId() != 1L)
                        .collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateUserRequest request)
    {
        var command = UserAdminConverter.toCreateUserCommand(request);
        userAppService.createUser(command, SecurityContextHelper.getUsername());
        return toAjax(true);
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateUserRequest request)
    {
        var command = UserAdminConverter.toUpdateUserCommand(request);
        userAppService.updateUser(command, SecurityContextHelper.getUsername());
        return success();
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, SecurityContextHelper.getUserId()))
        {
            return error("当前用户不能删除");
        }
        userAppService.deleteUser(userIds);
        return success();
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody ResetPwdRequest request)
    {
        var command = UserAdminConverter.toResetPwdCommand(request);
        userAppService.resetPwd(command);
        return success();
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody ChangeUserStatusRequest request)
    {
        var command = UserAdminConverter.toChangeUserStatusCommand(request);
        userAppService.changeStatus(command);
        return success();
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        UserResult user = userAppService.getUserResult(userId);
        List<RoleResult> roles = roleAppService.selectRoleResultsByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SecurityContextHelper.isAdmin(userId) ? roles
                : roles.stream().filter(role -> role.roleId() == null || role.roleId() != 1L)
                        .collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userAppService.authRole(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDeptTreeRequest request)
    {
        return success(TreeSelectAdminConverter.toVoList(
                deptAppService.listDeptTree(DeptAdminConverter.toQuery(request))));
    }
}
