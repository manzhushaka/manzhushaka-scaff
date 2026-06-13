package com.manzhushaka.system.service.impexp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 定义 ImportExportTaskSupport。
 */
public final class ImportExportTaskSupport {
    public static final String TASK_TYPE_EXPORT = "EXPORT";
    public static final String TASK_TYPE_IMPORT = "IMPORT";
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_PROCESSING = "PROCESSING";
    public static final String TASK_STATUS_SUCCESS = "SUCCESS";
    public static final String TASK_STATUS_FAIL = "FAIL";
    public static final String FILE_ROLE_SOURCE = "SOURCE";
    public static final String FILE_ROLE_RESULT = "RESULT";
    /**
     * 执行 of Pattern 逻辑。
     *
     * @param "yyyyMMddHHmmss" "yyyyMMddHHmmss" 参数
     * @return 处理结果
     */
    private static final DateTimeFormatter TASK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 创建 ImportExportTaskSupport 实例。
     */
    private ImportExportTaskSupport() {
    }

    /**
     * 执行 next Task No 逻辑。
     *
     * @param prefix prefix 参数
     * @return 处理结果
     */
    public static String nextTaskNo(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + "-" + LocalDateTime.now().format(TASK_NO_FORMATTER) + "-" + suffix;
    }
}
