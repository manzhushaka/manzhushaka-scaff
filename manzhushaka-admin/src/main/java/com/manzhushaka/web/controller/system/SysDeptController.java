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
import com.manzhushaka.system.application.service.SystemDeptAppService;
import com.manzhushaka.web.converter.system.DeptAdminConverter;
import com.manzhushaka.web.converter.system.shared.TreeSelectAdminConverter;
import com.manzhushaka.web.dto.system.DeptSaveRequest;
import com.manzhushaka.web.dto.system.SysDeptTreeRequest;

/**
 * 部门信息
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    @Autowired
    private SystemDeptAppService deptAppService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public AjaxResult list(SysDeptTreeRequest request)
    {
        return success(deptAppService.listDeptResults(DeptAdminConverter.toQuery(request)));
    }

    /**
     * 获取部门下拉树
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/tree")
    public AjaxResult tree(SysDeptTreeRequest request)
    {
        return success(TreeSelectAdminConverter.toVoList(
                deptAppService.listDeptTree(DeptAdminConverter.toQuery(request))));
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        return success(deptAppService.listDeptResultsExcluding(deptId));
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
        return success(deptAppService.getDeptResult(deptId));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DeptSaveRequest request)
    {
        return toAjax(deptAppService.createDept(DeptAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DeptSaveRequest request)
    {
        return toAjax(deptAppService.updateDept(DeptAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 保存部门排序
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "保存部门排序", businessType = BusinessType.UPDATE)
    @PutMapping("/updateSort")
    public AjaxResult updateSort(@RequestBody Map<String, String> params)
    {
        String[] deptIds = params.get("deptIds").split(",");
        String[] orderNums = params.get("orderNums").split(",");
        deptAppService.updateDeptSort(deptIds, orderNums);
        return success();
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        return toAjax(deptAppService.deleteDept(deptId));
    }
}
