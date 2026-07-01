package com.manzhushaka.quartz.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;
import com.manzhushaka.common.core.domain.BaseEntity;
import com.manzhushaka.common.utils.StringUtils;

/**
 * 定时任务调度过程日志明细表 sys_job_log_detail。
 *
 * @author manzhushaka
 * @date 2026-06-30
 */
public class SysJobLogDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细主键 */
    @Excel(name = "明细编号", cellType = ColumnType.NUMERIC)
    private Long detailId;

    /** 任务日志ID */
    @Excel(name = "任务日志ID", cellType = ColumnType.NUMERIC)
    private Long jobLogId;

    /** 日志级别 */
    @Excel(name = "日志级别")
    private String logLevel;

    /** 日志内容 */
    @Excel(name = "日志内容")
    private String logContent;

    /** 排序号 */
    @Excel(name = "排序号", cellType = ColumnType.NUMERIC)
    private Integer sortNo;

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    public Long getJobLogId()
    {
        return jobLogId;
    }

    public void setJobLogId(Long jobLogId)
    {
        this.jobLogId = jobLogId;
    }

    public String getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
    }

    public String getLogContent()
    {
        return logContent;
    }

    public void setLogContent(String logContent)
    {
        this.logContent = logContent;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("detailId", getDetailId())
                .append("jobLogId", getJobLogId())
                .append("logLevel", getLogLevel())
                .append("logContent", StringUtils.substring(getLogContent(), 0, 256))
                .append("sortNo", getSortNo())
                .append("createTime", getCreateTime())
                .toString();
    }
}
