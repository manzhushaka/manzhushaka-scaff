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

/**
 * 提供 ImportExportTaskController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/io-tasks", "/api/system/io-tasks"})
public class ImportExportTaskController {

    private final ImportExportTaskService importExportTaskService;

    /**
     * 创建 ImportExportTaskController 实例。
     *
     * @param importExportTaskService importExportTaskService 参数
     */
    public ImportExportTaskController(ImportExportTaskService importExportTaskService) {
        this.importExportTaskService = importExportTaskService;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping
    @SaCheckPermission(value = {"system:io:export:list", "system:io:import:list"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<PageResult<ImportExportTaskVO>> page(ImportExportTaskQuery query) {
        return ApiResponse.success(importExportTaskService.page(query));
    }

    /**
     * 执行 scene Options 逻辑。
     *
     * @param taskType taskType 参数
     * @return 处理结果
     */
    @GetMapping("/scenes")
    @SaCheckPermission(value = {"system:io:export:list", "system:io:import:list"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> sceneOptions(@RequestParam String taskType) {
        return ApiResponse.success(importExportTaskService.sceneOptions(taskType));
    }

    /**
     * 生成下载地址。
     *
     * @param id 主键 ID
     * @param fileRole fileRole 参数
     * @return 创建结果
     */
    @GetMapping("/{id}/download-url")
    @SaCheckPermission(value = {"system:io:export:download", "system:io:import:download"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<DownloadUrlVO> generateDownloadUrl(@PathVariable Long id, @RequestParam String fileRole) {
        return ApiResponse.success(importExportTaskService.generateDownloadUrl(id, fileRole));
    }
}
