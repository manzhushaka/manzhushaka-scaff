package com.manzhushaka.system.dto.job;

import com.manzhushaka.system.dto.PageQuery;

public class PlatformJobLogQuery extends PageQuery {
    private String runStatus;
    private String triggerType;

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
}
