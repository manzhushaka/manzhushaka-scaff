package com.manzhushaka.system.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;
import com.manzhushaka.common.core.domain.BaseEntity;
import com.manzhushaka.common.utils.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 慢 SQL 日志记录表 sys_slow_sql_log。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class SysSlowSqlLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 慢 SQL 主键 */
    @Excel(name = "慢SQL编号", cellType = ColumnType.NUMERIC)
    private Long slowSqlId;

    /** Mapper 方法 */
    @Excel(name = "Mapper方法")
    private String mapperId;

    /** SQL 文本 */
    private String sqlText;

    /** 数据源名称 */
    @Excel(name = "数据源")
    private String dataSourceName;

    /** 消耗时间 */
    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;

    /** 错误消息 */
    private String errorMsg;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;

    public Long getSlowSqlId()
    {
        return slowSqlId;
    }

    public void setSlowSqlId(Long slowSqlId)
    {
        this.slowSqlId = slowSqlId;
    }

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

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Date getExecuteTime()
    {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime)
    {
        this.executeTime = executeTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("slowSqlId", getSlowSqlId())
                .append("mapperId", getMapperId())
                .append("sqlText", StringUtils.substring(getSqlText(), 0, 512))
                .append("dataSourceName", getDataSourceName())
                .append("costTime", getCostTime())
                .append("errorMsg", StringUtils.substring(getErrorMsg(), 0, 256))
                .append("executeTime", getExecuteTime())
                .toString();
    }
}
