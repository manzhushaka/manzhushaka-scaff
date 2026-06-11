package com.manzhushaka.system.vo.monitor;

import java.util.List;

public class MonitorLogTailVO {
    private Boolean available;
    private String generatedAt;
    private String lastEntryAt;
    private List<String> lines;

    /**
     * 返回在线日志是否可用。
     *
     * @return 在线日志是否可用
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * 设置在线日志是否可用。
     *
     * @param available 在线日志是否可用
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     * 返回生成时间。
     *
     * @return 生成时间
     */
    public String getGeneratedAt() {
        return generatedAt;
    }

    /**
     * 设置生成时间。
     *
     * @param generatedAt 生成时间
     */
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * 返回最后一条日志时间。
     *
     * @return 最后一条日志时间
     */
    public String getLastEntryAt() {
        return lastEntryAt;
    }

    /**
     * 设置最后一条日志时间。
     *
     * @param lastEntryAt 最后一条日志时间
     */
    public void setLastEntryAt(String lastEntryAt) {
        this.lastEntryAt = lastEntryAt;
    }

    /**
     * 返回日志文本列表。
     *
     * @return 日志文本列表
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * 设置日志文本列表。
     *
     * @param lines 日志文本列表
     */
    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
