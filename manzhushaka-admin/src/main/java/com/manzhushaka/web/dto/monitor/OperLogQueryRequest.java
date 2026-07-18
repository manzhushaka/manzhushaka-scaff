package com.manzhushaka.web.dto.monitor;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 操作日志查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class OperLogQueryRequest extends DateRangeRequest
{
    private String title;
    private Integer businessType;
    private Integer[] businessTypes;
    private Integer status;
    private String operName;
    private String operIp;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(Integer businessType)
    {
        this.businessType = businessType;
    }

    public Integer[] getBusinessTypes()
    {
        return businessTypes;
    }

    public void setBusinessTypes(Integer[] businessTypes)
    {
        this.businessTypes = businessTypes;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    public String getOperIp()
    {
        return operIp;
    }

    public void setOperIp(String operIp)
    {
        this.operIp = operIp;
    }
}
