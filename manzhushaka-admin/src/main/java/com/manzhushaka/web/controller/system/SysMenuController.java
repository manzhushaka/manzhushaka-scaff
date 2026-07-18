package com.manzhushaka.web.controller.system;

import java.util.Map;
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
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.system.application.service.SystemMenuAppService;
import com.manzhushaka.web.converter.system.MenuAdminConverter;
import com.manzhushaka.web.converter.system.shared.TreeSelectAdminConverter;
import com.manzhushaka.web.dto.system.MenuQueryRequest;
import com.manzhushaka.web.dto.system.MenuSaveRequest;

/**
 * 菜单信息
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController
{
    @Autowired
    private SystemMenuAppService menuAppService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(MenuQueryRequest request)
    {
        return success(menuAppService.listMenuResults(MenuAdminConverter.toQuery(request),
                SecurityContextHelper.getUserId()));
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId)
    {
        return success(menuAppService.getMenuResult(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(MenuQueryRequest request)
    {
        return success(TreeSelectAdminConverter.toVoList(menuAppService.listMenuTree(
                MenuAdminConverter.toQuery(request), SecurityContextHelper.getUserId())));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuAppService.listCheckedMenuIds(roleId));
        ajax.put("menus", TreeSelectAdminConverter.toVoList(menuAppService.listMenuTree(
                MenuAdminConverter.toQuery(null), SecurityContextHelper.getUserId())));
        return ajax;
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MenuSaveRequest request)
    {
        return toAjax(menuAppService.createMenu(MenuAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MenuSaveRequest request)
    {
        return toAjax(menuAppService.updateMenu(MenuAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 保存菜单排序
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "保存菜单排序", businessType = BusinessType.UPDATE)
    @PutMapping("/updateSort")
    public AjaxResult updateSort(@RequestBody Map<String, String> params)
    {
        String[] menuIds = params.get("menuIds").split(",");
        String[] orderNums = params.get("orderNums").split(",");
        menuAppService.updateMenuSort(menuIds, orderNums);
        return success();
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId)
    {
        return toAjax(menuAppService.deleteMenu(menuId));
    }
}
