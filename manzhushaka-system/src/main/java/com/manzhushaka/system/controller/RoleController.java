package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.role.RoleForm;
import com.manzhushaka.system.dto.role.RoleQuery;
import com.manzhushaka.system.service.RoleService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.role.RoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供 RoleController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/roles", "/api/system/roles"})
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建 RoleController 实例。
     *
     * @param roleService roleService 参数
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping
    @SaCheckPermission("system:role:list")
    public ApiResponse<PageResult<RoleVO>> page(RoleQuery query) {
        return ApiResponse.success(roleService.page(query));
    }

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    @GetMapping("/options")
    @SaCheckPermission(value = {"system:role:list", "system:user:add", "system:user:update"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> options() {
        return ApiResponse.success(roleService.options());
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:role:query")
    public ApiResponse<RoleVO> getById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping
    @SaCheckPermission("system:role:add")
    public ApiResponse<Long> create(@Valid @RequestBody RoleForm form) {
        return ApiResponse.success(roleService.create(form));
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:update")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleForm form) {
        roleService.update(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success(null);
    }
}
