package com.manzhushaka.web.dto.monitor;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 消息队列台账查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MqMessageLogQueryRequest extends DateRangeRequest
{
    private String messageType;
    private String streamKey;
    private String businessKey;
    private String status;

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getStreamKey()
    {
        return streamKey;
    }

    public void setStreamKey(String streamKey)
    {
        this.streamKey = streamKey;
    }

    public String getBusinessKey()
    {
        return businessKey;
    }

    public void setBusinessKey(String businessKey)
    {
        this.businessKey = businessKey;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
