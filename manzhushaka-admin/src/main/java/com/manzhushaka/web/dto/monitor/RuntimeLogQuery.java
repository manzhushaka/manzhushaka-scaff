package com.manzhushaka.web.dto.monitor;

/**
 * 运行日志查询条件。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RuntimeLogQuery {

    /** 日志文件名 */
    private String fileName;

    /** 日志级别 */
    private String level;

    /** 关键字 */
    private String keyword;

    /** 读取行数 */
    private Integer lineCount;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }
}
