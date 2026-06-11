package com.manzhushaka.db.monitor;

import java.time.LocalDateTime;

public class SlowSqlRecord {
    private String statementId;
    private String sql;
    private long costMs;
    private Integer resultSize;
    private LocalDateTime executeTime;

    /**
     * 返回语句标识。
     *
     * @return 语句标识
     */
    public String getStatementId() {
        return statementId;
    }

    /**
     * 设置语句标识。
     *
     * @param statementId 语句标识
     */
    public void setStatementId(String statementId) {
        this.statementId = statementId;
    }

    /**
     * 返回 SQL 文本。
     *
     * @return SQL 文本
     */
    public String getSql() {
        return sql;
    }

    /**
     * 设置 SQL 文本。
     *
     * @param sql SQL 文本
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * 返回执行耗时。
     *
     * @return 执行耗时，单位毫秒
     */
    public long getCostMs() {
        return costMs;
    }

    /**
     * 设置执行耗时。
     *
     * @param costMs 执行耗时，单位毫秒
     */
    public void setCostMs(long costMs) {
        this.costMs = costMs;
    }

    /**
     * 返回结果规模。
     *
     * @return 结果规模
     */
    public Integer getResultSize() {
        return resultSize;
    }

    /**
     * 设置结果规模。
     *
     * @param resultSize 结果规模
     */
    public void setResultSize(Integer resultSize) {
        this.resultSize = resultSize;
    }

    /**
     * 返回执行时间。
     *
     * @return 执行时间
     */
    public LocalDateTime getExecuteTime() {
        return executeTime;
    }

    /**
     * 设置执行时间。
     *
     * @param executeTime 执行时间
     */
    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }
}
