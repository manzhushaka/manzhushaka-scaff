package com.manzhushaka.web.dto.iip;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 积分流水查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class PointsRecordQueryRequest extends DateRangeRequest
{
    private Long memberId;
    private String changeType;
    private String bizType;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }
}
