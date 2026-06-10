package com.manzhushaka.system.controller;

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

@RestController
@RequestMapping({"/system/jobs", "/api/system/jobs"})
public class PlatformJobController {

    private final PlatformJobService platformJobService;

    public PlatformJobController(PlatformJobService platformJobService) {
        this.platformJobService = platformJobService;
    }

    @GetMapping
    public ApiResponse<PageResult<PlatformJobVO>> page(PlatformJobQuery query) {
        return ApiResponse.success(platformJobService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlatformJobVO> getById(@PathVariable Long id) {
        return ApiResponse.success(platformJobService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody PlatformJobForm form) {
        return ApiResponse.success(platformJobService.create(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PlatformJobForm form) {
        platformJobService.update(id, form);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        platformJobService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<Void> pause(@PathVariable Long id) {
        platformJobService.pause(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<Void> resume(@PathVariable Long id) {
        platformJobService.resume(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/trigger")
    public ApiResponse<Void> trigger(@PathVariable Long id) {
        platformJobService.trigger(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/handlers/options")
    public ApiResponse<List<LabelValueOption>> handlerOptions() {
        return ApiResponse.success(platformJobService.handlerOptions());
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<PageResult<PlatformJobLogVO>> pageLogs(@PathVariable Long id, PlatformJobLogQuery query) {
        return ApiResponse.success(platformJobService.pageLogs(id, query));
    }

    @GetMapping("/logs/{logId}")
    public ApiResponse<PlatformJobLogDetailVO> getLogDetail(@PathVariable Long logId) {
        return ApiResponse.success(platformJobService.getLogDetail(logId));
    }
}
