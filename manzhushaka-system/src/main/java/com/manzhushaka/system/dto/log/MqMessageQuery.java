package com.manzhushaka.system.dto.log;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 MqMessageQuery 请求参数。
 */
public class MqMessageQuery extends PageQuery {
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String status;
    private String source;

    /**
     * 返回 streamKey。
     *
     * @return 字段值
     */
    public String getStreamKey() {
        return streamKey;
    }

    /**
     * 设置 streamKey。
     *
     * @param streamKey streamKey 参数
     */
    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    /**
     * 返回 eventType。
     *
     * @return 字段值
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 设置 eventType。
     *
     * @param eventType eventType 参数
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * 返回 bizKey。
     *
     * @return 字段值
     */
    public String getBizKey() {
        return bizKey;
    }

    /**
     * 设置 bizKey。
     *
     * @param bizKey bizKey 参数
     */
    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
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
     * 返回 status。
     *
     * @return 字段值
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 返回 source。
     *
     * @return 字段值
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置 source。
     *
     * @param source source 参数
     */
    public void setSource(String source) {
        this.source = source;
    }
}
