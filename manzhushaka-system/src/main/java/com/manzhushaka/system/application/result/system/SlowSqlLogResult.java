package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 慢 SQL 日志结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SlowSqlLogResult(
        @Excel(name = "慢SQL编号", cellType = ColumnType.NUMERIC) Long slowSqlId,
        @Excel(name = "Mapper方法") String mapperId,
        String sqlText,
        @Excel(name = "数据源") String dataSourceName,
        @Excel(name = "消耗时间", suffix = "毫秒") Long costTime,
        String errorMsg,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date executeTime)
{
    @Override
    public String toString()
    {
        return "SlowSqlLogResult[slowSqlId=" + slowSqlId + ", mapperId=" + mapperId
                + ", dataSourceName=" + dataSourceName + ", costTime=" + costTime + "]";
    }
}
