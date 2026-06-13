package com.manzhushaka.system.vo.job;

/**
 * 承载 PlatformJobLogDetailVO 响应数据。
 */
public class PlatformJobLogDetailVO extends PlatformJobLogVO {
    private String logContent;

    /**
     * 返回 logContent。
     *
     * @return 字段值
     */
    public String getLogContent() {
        return logContent;
    }

    /**
     * 设置 logContent。
     *
     * @param logContent logContent 参数
     */
    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }
}
