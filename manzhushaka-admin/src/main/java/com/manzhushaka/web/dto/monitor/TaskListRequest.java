package com.manzhushaka.web.dto.monitor;

/**
 * 异步任务列表查询请求。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public class TaskListRequest
{
    private String status;

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
