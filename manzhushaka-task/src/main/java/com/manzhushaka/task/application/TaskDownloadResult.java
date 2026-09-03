package com.manzhushaka.task.application;

import java.nio.file.Path;

/**
 * 导出文件下载结果。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public record TaskDownloadResult(Path path, String fileName, String contentType)
{
}
