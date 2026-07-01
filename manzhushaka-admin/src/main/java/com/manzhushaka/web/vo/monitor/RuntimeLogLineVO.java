package com.manzhushaka.web.vo.monitor;

/**
 * 运行日志行展示对象。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RuntimeLogLineVO {

    /** 行号 */
    private Long lineNumber;

    /** 日志级别 */
    private String level;

    /** 日志时间 */
    private String time;

    /** 日志内容 */
    private String content;

    /** 异常堆栈块 */
    private String stackTraceBlock;

    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStackTraceBlock() {
        return stackTraceBlock;
    }

    public void setStackTraceBlock(String stackTraceBlock) {
        this.stackTraceBlock = stackTraceBlock;
    }
}
