package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.impexp.ExportTaskCreateForm;
import com.manzhushaka.system.dto.impexp.ImportExportTaskQuery;
import com.manzhushaka.system.dto.impexp.ImportTaskCreateCommand;
import com.manzhushaka.system.service.ImportExportTaskService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.impexp.DownloadUrlVO;
import com.manzhushaka.system.vo.impexp.ImportExportTaskVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping({"/system/io-tasks", "/api/system/io-tasks"})
public class ImportExportTaskController {

    private final ImportExportTaskService importExportTaskService;

    public ImportExportTaskController(ImportExportTaskService importExportTaskService) {
        this.importExportTaskService = importExportTaskService;
    }

    @GetMapping
    public ApiResponse<PageResult<ImportExportTaskVO>> page(ImportExportTaskQuery query) {
        return ApiResponse.success(importExportTaskService.page(query));
    }

    @GetMapping("/scenes")
    public ApiResponse<List<LabelValueOption>> sceneOptions(@RequestParam String taskType) {
        return ApiResponse.success(importExportTaskService.sceneOptions(taskType));
    }

    @PostMapping("/exports")
    public ApiResponse<Long> createExportTask(@Valid @RequestBody ExportTaskCreateForm form) {
        return ApiResponse.success(importExportTaskService.createExportTask(form));
    }

    @PostMapping("/imports")
    public ApiResponse<Long> createImportTask(
        @RequestParam String bizType,
        @RequestParam(required = false) String taskName,
        @RequestPart("file") MultipartFile file
    ) throws IOException {
        ImportTaskCreateCommand command = new ImportTaskCreateCommand(
            bizType,
            taskName,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes()
        );
        return ApiResponse.success(importExportTaskService.createImportTask(command));
    }

    @GetMapping("/{id}/download-url")
    public ApiResponse<DownloadUrlVO> generateDownloadUrl(@PathVariable Long id, @RequestParam String fileRole) {
        return ApiResponse.success(importExportTaskService.generateDownloadUrl(id, fileRole));
    }
}
