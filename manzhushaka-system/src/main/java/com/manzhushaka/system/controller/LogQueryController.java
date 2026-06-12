package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.mq.service.MqMessageAdminService;
import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.service.LogQueryService;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.MqMessageVO;
import com.manzhushaka.system.vo.log.OpLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/system/logs", "/api/system/logs"})
public class LogQueryController {

    private final LogQueryService logQueryService;
    private final MqMessageAdminService mqMessageAdminService;

    public LogQueryController(LogQueryService logQueryService, MqMessageAdminService mqMessageAdminService) {
        this.logQueryService = logQueryService;
        this.mqMessageAdminService = mqMessageAdminService;
    }

    @GetMapping("/login")
    @SaCheckPermission("system:log:view")
    public ApiResponse<PageResult<LoginLogVO>> pageLoginLogs(LoginLogQuery query) {
        return ApiResponse.success(logQueryService.pageLoginLogs(query));
    }

    @GetMapping("/op")
    @SaCheckPermission("system:log:view")
    public ApiResponse<PageResult<OpLogVO>> pageOpLogs(OpLogQuery query) {
        return ApiResponse.success(logQueryService.pageOpLogs(query));
    }

    @GetMapping("/mq-messages")
    @SaCheckPermission("system:mq-message:query")
    public ApiResponse<PageResult<MqMessageVO>> pageMqMessages(MqMessageQuery query) {
        return ApiResponse.success(logQueryService.pageMqMessages(query));
    }

    @PostMapping("/mq-messages/{id}/retry")
    @SaCheckPermission("system:mq-message:retry")
    public ApiResponse<Void> retryMqMessage(@PathVariable("id") Long id) {
        mqMessageAdminService.retry(id);
        return ApiResponse.success(null);
    }
}
