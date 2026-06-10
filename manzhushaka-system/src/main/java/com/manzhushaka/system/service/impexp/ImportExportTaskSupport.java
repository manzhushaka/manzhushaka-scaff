package com.manzhushaka.system.service.impexp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ImportExportTaskSupport {
    public static final String TASK_TYPE_EXPORT = "EXPORT";
    public static final String TASK_TYPE_IMPORT = "IMPORT";
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_PROCESSING = "PROCESSING";
    public static final String TASK_STATUS_SUCCESS = "SUCCESS";
    public static final String TASK_STATUS_FAIL = "FAIL";
    public static final String FILE_ROLE_SOURCE = "SOURCE";
    public static final String FILE_ROLE_RESULT = "RESULT";
    private static final DateTimeFormatter TASK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private ImportExportTaskSupport() {
    }

    public static String nextTaskNo(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + "-" + LocalDateTime.now().format(TASK_NO_FORMATTER) + "-" + suffix;
    }
}
