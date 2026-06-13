package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.service.impl.ServerMonitorService;
import com.manzhushaka.system.vo.monitor.MonitorLogTailVO;
import com.manzhushaka.system.vo.monitor.MonitorSlowSqlVO;
import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供 ServerMonitorController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/monitor", "/api/system/monitor"})
public class ServerMonitorController {

    private final ServerMonitorService serverMonitorService;

    /**
     * 创建 ServerMonitorController 实例。
     *
     * @param serverMonitorService serverMonitorService 参数
     */
    public ServerMonitorController(ServerMonitorService serverMonitorService) {
        this.serverMonitorService = serverMonitorService;
    }

    /**
     * 查询运行监控总览。
     *
     * @return 运行监控总览
     */
    @GetMapping("/server")
    @SaCheckPermission("system:monitor:view")
    public ApiResponse<ServerMonitorVO> getServerMonitor() {
        return ApiResponse.success(serverMonitorService.getServerMonitor());
    }

    /**
     * 查询最近慢 SQL 列表。
     *
     * @param limit 返回条数
     * @return 最近慢 SQL 列表
     */
    @GetMapping("/slow-sql")
    @SaCheckPermission("system:monitor:view")
    public ApiResponse<List<MonitorSlowSqlVO>> listSlowSql(@RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(serverMonitorService.listSlowSqlRecords(limit));
    }

    /**
     * 查询在线日志 tail。
     *
     * @param limit 返回条数
     * @return 在线日志 tail
     */
    @GetMapping("/logs/tail")
    @SaCheckPermission("system:monitor:view")
    public ApiResponse<MonitorLogTailVO> getLogTail(@RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(serverMonitorService.getLogTail(limit));
    }
}
