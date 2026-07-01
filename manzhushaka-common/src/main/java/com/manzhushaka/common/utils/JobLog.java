package com.manzhushaka.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 定时任务过程日志。
 *
 * @author manzhushaka
 * @date 2026-06-30
 */
public final class JobLog
{
    /** 单条过程日志最大长度 */
    public static final int MAX_CONTENT_LENGTH = 1000;

    private static final ThreadLocal<List<Line>> LOG_LINES = new ThreadLocal<>();

    private JobLog()
    {
    }

    /**
     * 开始收集当前线程的定时任务过程日志。
     */
    public static void start()
    {
        LOG_LINES.set(new ArrayList<>());
    }

    /**
     * 记录普通过程日志。
     *
     * @param template 日志模板
     * @param params 日志参数
     */
    public static void info(String template, Object... params)
    {
        append("INFO", template, params);
    }

    /**
     * 记录警告过程日志。
     *
     * @param template 日志模板
     * @param params 日志参数
     */
    public static void warn(String template, Object... params)
    {
        append("WARN", template, params);
    }

    /**
     * 记录错误过程日志。
     *
     * @param template 日志模板
     * @param params 日志参数
     */
    public static void error(String template, Object... params)
    {
        append("ERROR", template, params);
    }

    /**
     * 获取当前线程已收集的过程日志。
     *
     * @return 过程日志列表
     */
    public static List<Line> getLines()
    {
        List<Line> lines = LOG_LINES.get();
        if (lines == null)
        {
            return Collections.emptyList();
        }
        return new ArrayList<>(lines);
    }

    /**
     * 清理当前线程过程日志。
     */
    public static void clear()
    {
        LOG_LINES.remove();
    }

    /**
     * 追加一条过程日志。
     *
     * @param logLevel 日志级别
     * @param template 日志模板
     * @param params 日志参数
     */
    private static void append(String logLevel, String template, Object... params)
    {
        List<Line> lines = LOG_LINES.get();
        if (lines == null)
        {
            return;
        }
        String content = StringUtils.format(template, params);
        content = StringUtils.substring(content, 0, MAX_CONTENT_LENGTH);
        lines.add(new Line(logLevel, content, lines.size() + 1));
    }

    /**
     * 定时任务过程日志行。
     *
     * @author manzhushaka
     * @date 2026-06-30
     */
    public static class Line
    {
        /** 日志级别 */
        private final String logLevel;

        /** 日志内容 */
        private final String logContent;

        /** 排序号 */
        private final Integer sortNo;

        /**
         * 构造定时任务过程日志行。
         *
         * @param logLevel 日志级别
         * @param logContent 日志内容
         * @param sortNo 排序号
         */
        public Line(String logLevel, String logContent, Integer sortNo)
        {
            this.logLevel = logLevel;
            this.logContent = logContent;
            this.sortNo = sortNo;
        }

        public String getLogLevel()
        {
            return logLevel;
        }

        public String getLogContent()
        {
            return logContent;
        }

        public Integer getSortNo()
        {
            return sortNo;
        }
    }
}
