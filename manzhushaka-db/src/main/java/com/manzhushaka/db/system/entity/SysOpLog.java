package com.manzhushaka.db.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

/**
 * 映射 SysOpLog 数据库实体。
 */
public class SysOpLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String traceId;
    private String module;
    private String action;
    private String businessType;
    private String requestUri;
    private String requestMethod;
    private Long operatorId;
    private String operatorName;
    private Long costMs;
    private Boolean success;
    private String errorMsg;
    private String requestSnapshot;
    private String responseSnapshot;
    private LocalDateTime createTime;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 返回 traceId。
     *
     * @return 字段值
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置 traceId。
     *
     * @param traceId traceId 标识
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

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
     * 返回 businessType。
     *
     * @return 字段值
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * 设置 businessType。
     *
     * @param businessType businessType 参数
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /**
     * 返回 requestUri。
     *
     * @return 字段值
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * 设置 requestUri。
     *
     * @param requestUri requestUri 参数
     */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    /**
     * 返回 requestMethod。
     *
     * @return 字段值
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * 设置 requestMethod。
     *
     * @param requestMethod requestMethod 参数
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 返回 operatorId。
     *
     * @return 字段值
     */
    public Long getOperatorId() {
        return operatorId;
    }

    /**
     * 设置 operatorId。
     *
     * @param operatorId operatorId 标识
     */
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
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
     * 返回 costMs。
     *
     * @return 字段值
     */
    public Long getCostMs() {
        return costMs;
    }

    /**
     * 设置 costMs。
     *
     * @param costMs costMs 参数
     */
    public void setCostMs(Long costMs) {
        this.costMs = costMs;
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

    /**
     * 返回 errorMsg。
     *
     * @return 字段值
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 设置 errorMsg。
     *
     * @param errorMsg errorMsg 参数
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 返回 requestSnapshot。
     *
     * @return 字段值
     */
    public String getRequestSnapshot() {
        return requestSnapshot;
    }

    /**
     * 设置 requestSnapshot。
     *
     * @param requestSnapshot requestSnapshot 参数
     */
    public void setRequestSnapshot(String requestSnapshot) {
        this.requestSnapshot = requestSnapshot;
    }

    /**
     * 返回 responseSnapshot。
     *
     * @return 字段值
     */
    public String getResponseSnapshot() {
        return responseSnapshot;
    }

    /**
     * 设置 responseSnapshot。
     *
     * @param responseSnapshot responseSnapshot 参数
     */
    public void setResponseSnapshot(String responseSnapshot) {
        this.responseSnapshot = responseSnapshot;
    }

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
