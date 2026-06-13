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

/**
 * 提供 LogQueryController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/logs", "/api/system/logs"})
public class LogQueryController {

    private final LogQueryService logQueryService;
    private final MqMessageAdminService mqMessageAdminService;

    /**
     * 创建 LogQueryController 实例。
     *
     * @param logQueryService logQueryService 参数
     * @param mqMessageAdminService mqMessageAdminService 参数
     */
    public LogQueryController(LogQueryService logQueryService, MqMessageAdminService mqMessageAdminService) {
        this.logQueryService = logQueryService;
        this.mqMessageAdminService = mqMessageAdminService;
    }

    /**
     * 查询 page Login Logs 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/login")
    @SaCheckPermission("system:log:view")
    public ApiResponse<PageResult<LoginLogVO>> pageLoginLogs(LoginLogQuery query) {
        return ApiResponse.success(logQueryService.pageLoginLogs(query));
    }

    /**
     * 查询 page Op Logs 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/op")
    @SaCheckPermission("system:log:view")
    public ApiResponse<PageResult<OpLogVO>> pageOpLogs(OpLogQuery query) {
        return ApiResponse.success(logQueryService.pageOpLogs(query));
    }

    /**
     * 查询 page Mq Messages 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/mq-messages")
    @SaCheckPermission("system:mq-message:query")
    public ApiResponse<PageResult<MqMessageVO>> pageMqMessages(MqMessageQuery query) {
        return ApiResponse.success(logQueryService.pageMqMessages(query));
    }

    /**
     * 重试消息。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @PostMapping("/mq-messages/{id}/retry")
    @SaCheckPermission("system:mq-message:retry")
    public ApiResponse<Void> retryMqMessage(@PathVariable("id") Long id) {
        mqMessageAdminService.retry(id);
        return ApiResponse.success(null);
    }
}
