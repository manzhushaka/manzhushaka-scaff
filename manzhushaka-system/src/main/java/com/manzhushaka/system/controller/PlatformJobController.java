package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.job.PlatformJobForm;
import com.manzhushaka.system.dto.job.PlatformJobLogQuery;
import com.manzhushaka.system.dto.job.PlatformJobQuery;
import com.manzhushaka.system.service.PlatformJobService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.job.PlatformJobLogDetailVO;
import com.manzhushaka.system.vo.job.PlatformJobLogVO;
import com.manzhushaka.system.vo.job.PlatformJobVO;
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

/**
 * 提供 PlatformJobController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/jobs", "/api/system/jobs"})
public class PlatformJobController {

    private final PlatformJobService platformJobService;

    /**
     * 创建 PlatformJobController 实例。
     *
     * @param platformJobService platformJobService 参数
     */
    public PlatformJobController(PlatformJobService platformJobService) {
        this.platformJobService = platformJobService;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping
    @SaCheckPermission("system:job:list")
    public ApiResponse<PageResult<PlatformJobVO>> page(PlatformJobQuery query) {
        return ApiResponse.success(platformJobService.page(query));
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:job:query")
    public ApiResponse<PlatformJobVO> getById(@PathVariable Long id) {
        return ApiResponse.success(platformJobService.getById(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping
    @SaCheckPermission("system:job:add")
    public ApiResponse<Long> create(@Valid @RequestBody PlatformJobForm form) {
        return ApiResponse.success(platformJobService.create(form));
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:job:update")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PlatformJobForm form) {
        platformJobService.update(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:job:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        platformJobService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 暂停任务。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/pause")
    @SaCheckPermission("system:job:pause")
    public ApiResponse<Void> pause(@PathVariable Long id) {
        platformJobService.pause(id);
        return ApiResponse.success(null);
    }

    /**
     * 恢复任务。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/resume")
    @SaCheckPermission("system:job:resume")
    public ApiResponse<Void> resume(@PathVariable Long id) {
        platformJobService.resume(id);
        return ApiResponse.success(null);
    }

    /**
     * 触发任务执行。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @PostMapping("/{id}/trigger")
    @SaCheckPermission("system:job:trigger")
    public ApiResponse<Void> trigger(@PathVariable Long id) {
        platformJobService.trigger(id);
        return ApiResponse.success(null);
    }

    /**
     * 处理 handler Options 流程。
     *
     * @return 处理结果
     */
    @GetMapping("/handlers/options")
    @SaCheckPermission(value = {"system:job:list", "system:job:add", "system:job:update"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> handlerOptions() {
        return ApiResponse.success(platformJobService.handlerOptions());
    }

    /**
     * 查询 page Logs 结果。
     *
     * @param id 主键 ID
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/{id}/logs")
    @SaCheckPermission("system:job:log")
    public ApiResponse<PageResult<PlatformJobLogVO>> pageLogs(@PathVariable Long id, PlatformJobLogQuery query) {
        return ApiResponse.success(platformJobService.pageLogs(id, query));
    }

    /**
     * 返回 logDetail。
     *
     * @param logId 日志 ID
     * @return 字段值
     */
    @GetMapping("/logs/{logId}")
    @SaCheckPermission("system:job:log")
    public ApiResponse<PlatformJobLogDetailVO> getLogDetail(@PathVariable Long logId) {
        return ApiResponse.success(platformJobService.getLogDetail(logId));
    }
}
