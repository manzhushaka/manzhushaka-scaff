package com.manzhushaka.system.application.result.system;

/**
 * 用户批量导入结果。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public record UserImportBatchResult(long successCount, long failureCount, String errorMessage)
{
}
