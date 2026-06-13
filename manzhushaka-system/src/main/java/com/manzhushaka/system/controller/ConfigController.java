package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.config.ConfigForm;
import com.manzhushaka.system.dto.config.ConfigQuery;
import com.manzhushaka.system.service.ConfigService;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.config.ConfigVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供 ConfigController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/configs", "/api/system/configs"})
public class ConfigController {

    private final ConfigService configService;

    /**
     * 创建 ConfigController 实例。
     *
     * @param configService configService 参数
     */
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping
    @SaCheckPermission("system:config:list")
    public ApiResponse<PageResult<ConfigVO>> page(ConfigQuery query) {
        return ApiResponse.success(configService.page(query));
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:config:query")
    public ApiResponse<ConfigVO> getById(@PathVariable Long id) {
        return ApiResponse.success(configService.getById(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping
    @SaCheckPermission("system:config:add")
    public ApiResponse<Long> create(@Valid @RequestBody ConfigForm form) {
        return ApiResponse.success(configService.create(form));
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:config:update")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigForm form) {
        configService.update(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:config:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResponse.success(null);
    }
}
