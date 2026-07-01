package com.manzhushaka.web.controller.monitor;

import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.utils.file.FileUtils;
import com.manzhushaka.web.dto.monitor.RuntimeLogQuery;
import com.manzhushaka.web.service.RuntimeLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 运行日志在线查看。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@RestController
@RequestMapping("/monitor/runtimeLog")
public class RuntimeLogController extends BaseController {

    @Autowired
    private RuntimeLogService runtimeLogService;

    @PreAuthorize("@ss.hasPermi('monitor:runtimelog:list')")
    @GetMapping("/files")
    public AjaxResult files() {
        return success(runtimeLogService.listFiles());
    }

    @PreAuthorize("@ss.hasPermi('monitor:runtimelog:list')")
    @GetMapping("/list")
    public AjaxResult list(RuntimeLogQuery query) {
        return success(runtimeLogService.list(query));
    }

    @PreAuthorize("@ss.hasPermi('monitor:runtimelog:query')")
    @GetMapping("/detail")
    public AjaxResult detail(RuntimeLogQuery query) {
        return success(runtimeLogService.list(query));
    }

    @PreAuthorize("@ss.hasPermi('monitor:runtimelog:download')")
    @GetMapping("/download")
    public void download(String fileName, HttpServletResponse response) throws Exception {
        Path path = runtimeLogService.resolveLogPath(fileName);
        response.setContentType("application/octet-stream");
        FileUtils.setAttachmentResponseHeader(response, fileName);
        Files.copy(path, response.getOutputStream());
    }
}
