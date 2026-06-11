package com.manzhushaka.db.monitor;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class SlowSqlMonitorStore {
    private static final int DEFAULT_CAPACITY = 100;
    private static final long DEFAULT_THRESHOLD_MS = 500L;
    private static final int SQL_TEXT_LIMIT = 600;

    private final int capacity;
    private final long thresholdMs;
    private final Deque<SlowSqlRecord> records = new ArrayDeque<>();

    /**
     * 使用默认阈值创建慢 SQL 存储器。
     *
     * @param capacity 缓冲区容量
     */
    public SlowSqlMonitorStore(int capacity) {
        this(capacity, DEFAULT_THRESHOLD_MS);
    }

    /**
     * 创建慢 SQL 存储器。
     *
     * @param capacity 缓冲区容量
     * @param thresholdMs 慢 SQL 阈值
     */
    public SlowSqlMonitorStore(int capacity, long thresholdMs) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
        this.thresholdMs = thresholdMs > 0 ? thresholdMs : DEFAULT_THRESHOLD_MS;
    }

    /**
     * 记录一条慢 SQL。
     *
     * @param statementId 语句标识
     * @param sql 原始 SQL
     * @param costMs 执行耗时
     * @param resultSize 结果规模
     */
    public synchronized void record(String statementId, String sql, long costMs, Integer resultSize) {
        SlowSqlRecord record = new SlowSqlRecord();
        record.setStatementId(statementId);
        record.setSql(normalizeSql(sql));
        record.setCostMs(costMs);
        record.setResultSize(resultSize);
        record.setExecuteTime(LocalDateTime.now());
        records.addLast(record);
        while (records.size() > capacity) {
            records.removeFirst();
        }
    }

    /**
     * 返回最近的慢 SQL 记录。
     *
     * @param limit 返回条数
     * @return 最近记录列表
     */
    public synchronized List<SlowSqlRecord> listRecent(int limit) {
        int normalizedLimit = normalizeLimit(limit);
        List<SlowSqlRecord> snapshot = new ArrayList<>(records);
        int fromIndex = Math.max(snapshot.size() - normalizedLimit, 0);
        List<SlowSqlRecord> recent = new ArrayList<>(snapshot.subList(fromIndex, snapshot.size()));
        Collections.reverse(recent);
        return recent;
    }

    /**
     * 返回当前记录数。
     *
     * @return 记录数
     */
    public synchronized int size() {
        return records.size();
    }

    /**
     * 返回最近一条记录。
     *
     * @return 最近记录
     */
    public synchronized SlowSqlRecord latest() {
        return records.peekLast();
    }

    /**
     * 返回慢 SQL 阈值。
     *
     * @return 阈值，单位毫秒
     */
    public long getThresholdMs() {
        return thresholdMs;
    }

    /**
     * 返回缓冲容量。
     *
     * @return 缓冲容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 规范化返回条数。
     *
     * @param limit 原始条数
     * @return 合法条数
     */
    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, capacity);
    }

    /**
     * 规范化 SQL 文本，便于监控页展示。
     *
     * @param sql 原始 SQL
     * @return 规整后的 SQL 文本
     */
    private String normalizeSql(String sql) {
        if (!StringUtils.hasText(sql)) {
            return "--";
        }
        String normalized = sql.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= SQL_TEXT_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, SQL_TEXT_LIMIT) + "...";
    }
}
