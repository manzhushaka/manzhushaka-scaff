package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.service.LogQueryService;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.OpLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/logs")
public class LogQueryController {

    private final LogQueryService logQueryService;

    public LogQueryController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping("/login")
    public ApiResponse<PageResult<LoginLogVO>> pageLoginLogs(LoginLogQuery query) {
        return ApiResponse.success(logQueryService.pageLoginLogs(query));
    }

    @GetMapping("/op")
    public ApiResponse<PageResult<OpLogVO>> pageOpLogs(OpLogQuery query) {
        return ApiResponse.success(logQueryService.pageOpLogs(query));
    }
}
