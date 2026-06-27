package com.manzhushaka.web.dto.system.user;

/**
 * 修改用户状态请求
 */
public class ChangeUserStatusRequest
{
    private Long userId;
    private String status;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
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