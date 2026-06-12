package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.service.impl.PlatformConfigService;
import com.manzhushaka.system.vo.config.PlatformConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/system/platform-config", "/api/system/platform-config"})
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    public PlatformConfigController(PlatformConfigService platformConfigService) {
        this.platformConfigService = platformConfigService;
    }

    @GetMapping
    @SaCheckPermission("system:config:query")
    public ApiResponse<PlatformConfigVO> getPlatformConfig() {
        return ApiResponse.success(platformConfigService.getPlatformConfig());
    }

    @PutMapping
    @SaCheckPermission("system:config:update")
    public ApiResponse<Void> savePlatformConfig(@RequestBody PlatformConfigVO payload) {
        platformConfigService.savePlatformConfig(payload);
        return ApiResponse.success(null);
    }
}
