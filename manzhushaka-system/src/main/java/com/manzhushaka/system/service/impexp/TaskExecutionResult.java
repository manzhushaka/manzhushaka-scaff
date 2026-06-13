package com.manzhushaka.system.service.impexp;

/**
 * 承载 TaskExecutionResult 数据。
 */
public record TaskExecutionResult(
    int totalCount,
    int successCount,
    int failCount,
    String message,
    TaskFileArtifact resultFile
) {

    public static TaskExecutionResult success(
        int totalCount,
        int successCount,
        int failCount,
        String message,
        TaskFileArtifact resultFile
    ) {
        return new TaskExecutionResult(totalCount, successCount, failCount, message, resultFile);
    }
}
