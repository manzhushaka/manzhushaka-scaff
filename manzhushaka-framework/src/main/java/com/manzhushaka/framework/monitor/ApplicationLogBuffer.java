package com.manzhushaka.framework.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 定义 ApplicationLogBuffer。
 */
@Component
public class ApplicationLogBuffer {
    private static final int DEFAULT_CAPACITY = 200;

    private final int capacity;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final Deque<BufferedLogLine> lines = new ArrayDeque<>();

    /**
     * 创建应用日志缓冲区。
     *
     * @param capacity 缓冲区容量
     */
    public ApplicationLogBuffer(@Value("${manzhushaka.monitor.log-tail.capacity:200}") int capacity) {
        this.capacity = capacity > 0 ? capacity : DEFAULT_CAPACITY;
    }

    /**
     * 追加一条日志。
     *
     * @param time 日志时间
     * @param line 日志文本
     */
    public synchronized void append(LocalDateTime time, String line) {
        lines.addLast(new BufferedLogLine(time, line));
        while (lines.size() > capacity) {
            lines.removeFirst();
        }
    }

    /**
     * 返回最近的日志文本。
     *
     * @param limit 返回条数
     * @return 日志文本列表
     */
    public synchronized List<String> tailLines(int limit) {
        int normalizedLimit = normalizeLimit(limit);
        List<BufferedLogLine> snapshot = new ArrayList<>(lines);
        int fromIndex = Math.max(snapshot.size() - normalizedLimit, 0);
        return snapshot.subList(fromIndex, snapshot.size()).stream()
            .map(BufferedLogLine::line)
            .toList();
    }

    /**
     * 返回当前缓冲条数。
     *
     * @return 缓冲条数
     */
    public synchronized int size() {
        return lines.size();
    }

    /**
     * 返回最近一条日志时间。
     *
     * @return 最近一条日志时间
     */
    public synchronized LocalDateTime lastEntryAt() {
        BufferedLogLine last = lines.peekLast();
        return last == null ? null : last.time();
    }

    /**
     * 返回缓冲区容量。
     *
     * @return 缓冲区容量
     */
    public int capacity() {
        return capacity;
    }

    /**
     * 规范化条数限制。
     *
     * @param limit 原始条数
     * @return 合法条数
     */
    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 80;
        }
        return Math.min(limit, capacity);
    }

    /**
     * 执行 Buffered Log Line 逻辑。
     *
     * @param time time 参数
     * @param line line 参数
     * @return 处理结果
     */
    private record BufferedLogLine(LocalDateTime time, String line) {
    }
}
