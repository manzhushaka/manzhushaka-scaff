package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.service.impl.ServerMonitorService;
import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/system/monitor", "/api/system/monitor"})
public class ServerMonitorController {

    private final ServerMonitorService serverMonitorService;

    public ServerMonitorController(ServerMonitorService serverMonitorService) {
        this.serverMonitorService = serverMonitorService;
    }

    @GetMapping("/server")
    public ApiResponse<ServerMonitorVO> getServerMonitor() {
        return ApiResponse.success(serverMonitorService.getServerMonitor());
    }
}
