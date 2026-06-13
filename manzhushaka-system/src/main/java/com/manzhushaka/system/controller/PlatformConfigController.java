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

/**
 * 提供 PlatformConfigController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/platform-config", "/api/system/platform-config"})
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    /**
     * 创建 PlatformConfigController 实例。
     *
     * @param platformConfigService platformConfigService 参数
     */
    public PlatformConfigController(PlatformConfigService platformConfigService) {
        this.platformConfigService = platformConfigService;
    }

    /**
     * 返回 platformConfig。
     *
     * @return 字段值
     */
    @GetMapping
    @SaCheckPermission("system:config:query")
    public ApiResponse<PlatformConfigVO> getPlatformConfig() {
        return ApiResponse.success(platformConfigService.getPlatformConfig());
    }

    /**
     * 更新 save Platform Config 数据。
     *
     * @param payload 请求数据
     * @return 处理结果
     */
    @PutMapping
    @SaCheckPermission("system:config:update")
    public ApiResponse<Void> savePlatformConfig(@RequestBody PlatformConfigVO payload) {
        platformConfigService.savePlatformConfig(payload);
        return ApiResponse.success(null);
    }
}
