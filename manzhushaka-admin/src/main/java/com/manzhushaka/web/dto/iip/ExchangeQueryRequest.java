package com.manzhushaka.web.dto.iip;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 兑换记录查询请求（管理端，params 携带兑换时间范围 beginTime/endTime）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class ExchangeQueryRequest extends DateRangeRequest
{
    private String couponName;

    private Long memberId;

    private String status;

    private String verifyCode;

    public String getCouponName()
    {
        return couponName;
    }

    public void setCouponName(String couponName)
    {
        this.couponName = couponName;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVerifyCode()
    {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode)
    {
        this.verifyCode = verifyCode;
    }
}
