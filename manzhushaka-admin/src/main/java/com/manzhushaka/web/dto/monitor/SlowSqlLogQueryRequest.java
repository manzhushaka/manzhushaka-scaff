package com.manzhushaka.web.dto.monitor;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 慢 SQL 日志查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class SlowSqlLogQueryRequest extends DateRangeRequest
{
    private String mapperId;
    private String sqlText;
    private String dataSourceName;
    private Long costTime;

    public String getMapperId()
    {
        return mapperId;
    }

    public void setMapperId(String mapperId)
    {
        this.mapperId = mapperId;
    }

    public String getSqlText()
    {
        return sqlText;
    }

    public void setSqlText(String sqlText)
    {
        this.sqlText = sqlText;
    }

    public String getDataSourceName()
    {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }
}
