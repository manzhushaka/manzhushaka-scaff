package com.manzhushaka.framework.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定义 JobLogger。
 */
public final class JobLogger {
    /**
     * 返回 logger。
     *
     * @param JobLogger.class JobLogger.class 参数
     * @return 字段值
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogger.class);
    /**
     * 执行 of Pattern 逻辑。
     *
     * @param HH:mm:ss" HH:mm:ss" 参数
     * @return 处理结果
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LOG_CONTENT_LENGTH = 1_000_000;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private static final ThreadLocal<JobLogBuffer> BUFFER_HOLDER = new ThreadLocal<>();

    /**
     * 创建 JobLogger 实例。
     */
    private JobLogger() {
    }

    /**
     * 执行 bind 逻辑。
     *
     * @param jobLogId jobLogId 标识
     */
    public static void bind(Long jobLogId) {
        BUFFER_HOLDER.set(new JobLogBuffer(jobLogId));
    }

    /**
     * 构建 collect And Clear 结果。
     *
     * @return 处理结果
     */
    public static String collectAndClear() {
        JobLogBuffer buffer = BUFFER_HOLDER.get();
        BUFFER_HOLDER.remove();
        return buffer == null ? "" : buffer.content();
    }

    /**
     * 执行 info 逻辑。
     *
     * @param template template 参数
     * @param args args 参数
     */
    public static void info(String template, Object... args) {
        write("INFO", template, args);
    }

    /**
     * 执行 warn 逻辑。
     *
     * @param template template 参数
     * @param args args 参数
     */
    public static void warn(String template, Object... args) {
        write("WARN", template, args);
    }

    /**
     * 执行 error 逻辑。
     *
     * @param template template 参数
     * @param args args 参数
     */
    public static void error(String template, Object... args) {
        write("ERROR", template, args);
    }

    /**
     * 执行 error 逻辑。
     *
     * @param message message 参数
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        write("ERROR", message, throwable);
    }

    /**
     * 更新 write 数据。
     *
     * @param level level 参数
     * @param template template 参数
     * @param args args 参数
     */
    private static void write(String level, String template, Object... args) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(template, args);
        write(level, tuple.getMessage(), tuple.getThrowable());
    }

    /**
     * 更新 write 数据。
     *
     * @param level level 参数
     * @param message message 参数
     * @param throwable 异常对象
     */
    private static void write(String level, String message, Throwable throwable) {
        logToApplication(level, message, throwable);

        JobLogBuffer buffer = BUFFER_HOLDER.get();
        if (buffer == null) {
            return;
        }

        StringBuilder builder = new StringBuilder()
            .append('[')
            .append(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .append("] [")
            .append(level)
            .append("] ")
            .append(message == null ? "" : message);
        if (throwable != null) {
            builder.append(System.lineSeparator()).append(stackTraceOf(throwable));
        }
        buffer.append(builder.toString(), MAX_LOG_CONTENT_LENGTH);
    }

    /**
     * 执行 log To Application 逻辑。
     *
     * @param level level 参数
     * @param message message 参数
     * @param throwable 异常对象
     */
    private static void logToApplication(String level, String message, Throwable throwable) {
        if ("ERROR".equals(level)) {
            if (throwable != null) {
                LOGGER.error(message, throwable);
            } else {
                LOGGER.error(message);
            }
            return;
        }
        if ("WARN".equals(level)) {
            if (throwable != null) {
                LOGGER.warn(message, throwable);
            } else {
                LOGGER.warn(message);
            }
            return;
        }
        if (throwable != null) {
            LOGGER.info(message, throwable);
        } else {
            LOGGER.info(message);
        }
    }

    /**
     * 执行 stack Trace Of 逻辑。
     *
     * @param throwable 异常对象
     * @return 处理结果
     */
    private static String stackTraceOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static final class JobLogBuffer {
        private final Long jobLogId;
        /**
         * 执行 String Builder 逻辑。
         *
         * @return 处理结果
         */
        private final StringBuilder content = new StringBuilder();
        private boolean truncated;

        /**
         * 执行 Job Log Buffer 逻辑。
         *
         * @param jobLogId jobLogId 标识
         * @return 处理结果
         */
        private JobLogBuffer(Long jobLogId) {
            this.jobLogId = jobLogId;
        }

        /**
         * 执行 append 逻辑。
         *
         * @param line line 参数
         * @param maxLength maxLength 参数
         */
        private void append(String line, int maxLength) {
            if (truncated) {
                return;
            }
            if (content.length() + line.length() + System.lineSeparator().length() > maxLength) {
                content.append("[")
                    .append(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                    .append("] [WARN] 日志内容已达到上限，后续内容已截断。")
                    .append(System.lineSeparator());
                truncated = true;
                return;
            }
            content.append(line).append(System.lineSeparator());
        }

        /**
         * 执行 content 逻辑。
         *
         * @return 处理结果
         */
        private String content() {
            return content.toString();
        }
    }
}
