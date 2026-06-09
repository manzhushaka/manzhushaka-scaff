package com.manzhushaka.system.dto.log;

import com.manzhushaka.system.dto.PageQuery;

public class OpLogQuery extends PageQuery {
    private String module;
    private String action;
    private String operatorName;
    private Boolean success;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
