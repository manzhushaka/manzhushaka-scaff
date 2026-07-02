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
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.web.converter.system.user.UserAdminConverter;
import com.manzhushaka.web.dto.system.user.ChangeUserStatusRequest;
import com.manzhushaka.web.dto.system.user.CreateUserRequest;
import com.manzhushaka.web.dto.system.user.ResetPwdRequest;
import com.manzhushaka.web.dto.system.user.UpdateUserRequest;
import com.manzhushaka.web.converter.system.shared.TreeSelectAdminConverter;
import com.manzhushaka.web.dto.system.user.UserListRequest;

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
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserListRequest request)
    {
        startPage();
        var query = UserAdminConverter.toUserListQuery(request);
        List<SysUser> list = userAppService.listUsers(query);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user)
    {
        List<SysUser> list = userAppService.exportUsers(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userAppService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
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
            SysUser sysUser = userAppService.getUserDetail(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SecurityContextHelper.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
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
        userAppService.createUser(command);
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
        userAppService.updateUser(command);
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
        if (ArrayUtils.contains(userIds, getUserId()))
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
        SysUser user = userAppService.getUserDetail(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SecurityContextHelper.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
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
    public AjaxResult deptTree(SysDept dept)
    {
        return success(TreeSelectAdminConverter.toVoList(deptService.selectDeptTreeList(dept)));
    }
}
