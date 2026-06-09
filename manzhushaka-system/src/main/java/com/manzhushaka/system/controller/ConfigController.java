package com.manzhushaka.system.controller;

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

@RestController
@RequestMapping("/system/configs")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ApiResponse<PageResult<ConfigVO>> page(ConfigQuery query) {
        return ApiResponse.success(configService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConfigVO> getById(@PathVariable Long id) {
        return ApiResponse.success(configService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ConfigForm form) {
        return ApiResponse.success(configService.create(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigForm form) {
        configService.update(id, form);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResponse.success(null);
    }
}
