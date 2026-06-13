package com.manzhushaka.system.dto.job;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 PlatformJobLogQuery 请求参数。
 */
public class PlatformJobLogQuery extends PageQuery {
    private String runStatus;
    private String triggerType;

    /**
     * 返回 runStatus。
     *
     * @return 字段值
     */
    public String getRunStatus() {
        return runStatus;
    }

    /**
     * 设置 runStatus。
     *
     * @param runStatus runStatus 参数
     */
    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    /**
     * 返回 triggerType。
     *
     * @return 字段值
     */
    public String getTriggerType() {
        return triggerType;
    }

    /**
     * 设置 triggerType。
     *
     * @param triggerType triggerType 参数
     */
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
}
