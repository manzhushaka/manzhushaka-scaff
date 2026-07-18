package com.manzhushaka.web.dto.monitor;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 登录日志查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class LoginLogQueryRequest extends DateRangeRequest
{
    private String ipaddr;
    private String status;
    private String userName;

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}
