package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.dept.DeptForm;
import com.manzhushaka.system.dto.dept.DeptQuery;
import com.manzhushaka.system.service.DeptService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.dept.DeptTreeVO;
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
 * 提供 DeptController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/depts", "/api/system/depts"})
public class DeptController {

    private final DeptService deptService;

    /**
     * 创建 DeptController 实例。
     *
     * @param deptService deptService 参数
     */
    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 执行 tree 逻辑。
     *
     * @param query 查询条件
     * @return 处理结果
     */
    @GetMapping("/tree")
    @SaCheckPermission("system:dept:list")
    public ApiResponse<List<DeptTreeVO>> tree(DeptQuery query) {
        return ApiResponse.success(deptService.tree(query));
    }

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    @GetMapping("/options")
    @SaCheckPermission(value = {"system:dept:list", "system:user:add", "system:user:update"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> options() {
        return ApiResponse.success(deptService.options());
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:dept:query")
    public ApiResponse<DeptTreeVO> getById(@PathVariable("id") Long id) {
        return ApiResponse.success(deptService.getById(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping
    @SaCheckPermission("system:dept:add")
    public ApiResponse<Long> create(@Valid @RequestBody DeptForm form) {
        return ApiResponse.success(deptService.create(form));
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:dept:update")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @Valid @RequestBody DeptForm form) {
        deptService.update(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:dept:delete")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        deptService.delete(id);
        return ApiResponse.success(null);
    }
}
