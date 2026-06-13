package com.manzhushaka.system.dto.log;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 OpLogQuery 请求参数。
 */
public class OpLogQuery extends PageQuery {
    private String module;
    private String action;
    private String operatorName;
    private Boolean success;

    /**
     * 返回 module。
     *
     * @return 字段值
     */
    public String getModule() {
        return module;
    }

    /**
     * 设置 module。
     *
     * @param module module 参数
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 返回 action。
     *
     * @return 字段值
     */
    public String getAction() {
        return action;
    }

    /**
     * 设置 action。
     *
     * @param action action 参数
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 返回 operatorName。
     *
     * @return 字段值
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 设置 operatorName。
     *
     * @param operatorName operatorName 参数
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * 返回 success。
     *
     * @return 字段值
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * 设置 success。
     *
     * @param success success 参数
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
