package com.manzhushaka.framework.web.command;

import java.util.Date;

/**
 * 慢 SQL 日志记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public record SlowSqlLogRecord(String mapperId, String sqlText, String dataSourceName, Long costTime,
                               String errorMsg, Date executeTime)
{
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * 慢 SQL 日志构建器。
     */
    public static final class Builder
    {
        private String mapperId;
        private String sqlText;
        private String dataSourceName;
        private Long costTime;
        private String errorMsg;
        private Date executeTime;

        public Builder mapperId(String mapperId)
        {
            this.mapperId = mapperId;
            return this;
        }

        public Builder sqlText(String sqlText)
        {
            this.sqlText = sqlText;
            return this;
        }

        public Builder dataSourceName(String dataSourceName)
        {
            this.dataSourceName = dataSourceName;
            return this;
        }

        public Builder costTime(Long costTime)
        {
            this.costTime = costTime;
            return this;
        }

        public Builder errorMsg(String errorMsg)
        {
            this.errorMsg = errorMsg;
            return this;
        }

        public Builder executeTime(Date executeTime)
        {
            this.executeTime = executeTime;
            return this;
        }

        public SlowSqlLogRecord build()
        {
            return new SlowSqlLogRecord(mapperId, sqlText, dataSourceName, costTime, errorMsg, executeTime);
        }
    }
}
