package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.impexp.ImportExportTaskQuery;
import com.manzhushaka.system.service.ImportExportTaskService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.impexp.DownloadUrlVO;
import com.manzhushaka.system.vo.impexp.ImportExportTaskVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping({"/system/io-tasks", "/api/system/io-tasks"})
public class ImportExportTaskController {

    private final ImportExportTaskService importExportTaskService;

    public ImportExportTaskController(ImportExportTaskService importExportTaskService) {
        this.importExportTaskService = importExportTaskService;
    }

    @GetMapping
    @SaCheckPermission(value = {"system:io:export:list", "system:io:import:list"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<PageResult<ImportExportTaskVO>> page(ImportExportTaskQuery query) {
        return ApiResponse.success(importExportTaskService.page(query));
    }

    @GetMapping("/scenes")
    @SaCheckPermission(value = {"system:io:export:list", "system:io:import:list"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> sceneOptions(@RequestParam String taskType) {
        return ApiResponse.success(importExportTaskService.sceneOptions(taskType));
    }

    @GetMapping("/{id}/download-url")
    @SaCheckPermission(value = {"system:io:export:download", "system:io:import:download"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<DownloadUrlVO> generateDownloadUrl(@PathVariable Long id, @RequestParam String fileRole) {
        return ApiResponse.success(importExportTaskService.generateDownloadUrl(id, fileRole));
    }
}
