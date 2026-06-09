package com.manzhushaka.system.controller;

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

@RestController
@RequestMapping("/system/depts")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<DeptTreeVO>> tree(DeptQuery query) {
        return ApiResponse.success(deptService.tree(query));
    }

    @GetMapping("/options")
    public ApiResponse<List<LabelValueOption>> options() {
        return ApiResponse.success(deptService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<DeptTreeVO> getById(@PathVariable Long id) {
        return ApiResponse.success(deptService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody DeptForm form) {
        return ApiResponse.success(deptService.create(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DeptForm form) {
        deptService.update(id, form);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ApiResponse.success(null);
    }
}
