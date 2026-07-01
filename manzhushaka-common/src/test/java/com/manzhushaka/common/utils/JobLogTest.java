package com.manzhushaka.common.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 定时任务过程日志测试。
 *
 * @author manzhushaka
 * @date 2026-06-30
 */
class JobLogTest
{
    @AfterEach
    void tearDown()
    {
        JobLog.clear();
    }

    @Test
    void shouldCollectFormattedLinesInCurrentThread()
    {
        JobLog.start();

        JobLog.info("开始处理订单：{}", "A001");
        JobLog.warn("订单{}库存不足", "A001");
        JobLog.error("订单{}处理失败", "A001");

        List<JobLog.Line> lines = JobLog.getLines();
        assertEquals(3, lines.size());
        assertEquals("INFO", lines.get(0).getLogLevel());
        assertEquals("开始处理订单：A001", lines.get(0).getLogContent());
        assertEquals(1, lines.get(0).getSortNo());
        assertEquals("WARN", lines.get(1).getLogLevel());
        assertEquals("订单A001库存不足", lines.get(1).getLogContent());
        assertEquals(2, lines.get(1).getSortNo());
        assertEquals("ERROR", lines.get(2).getLogLevel());
        assertEquals("订单A001处理失败", lines.get(2).getLogContent());
        assertEquals(3, lines.get(2).getSortNo());
    }

    @Test
    void shouldIgnoreWritesWhenCollectorNotStarted()
    {
        assertDoesNotThrow(() -> JobLog.info("不在定时任务中"));
        assertTrue(JobLog.getLines().isEmpty());
    }

    @Test
    void shouldClearCurrentThreadLines()
    {
        JobLog.start();
        JobLog.info("第一次执行");
        JobLog.clear();

        JobLog.start();
        JobLog.info("第二次执行");

        List<JobLog.Line> lines = JobLog.getLines();
        assertEquals(1, lines.size());
        assertEquals("第二次执行", lines.get(0).getLogContent());
        assertEquals(1, lines.get(0).getSortNo());
    }

    @Test
    void shouldLimitSingleLineLength()
    {
        JobLog.start();

        JobLog.info(StringUtils.repeat("A", 1001));

        assertEquals(1000, JobLog.getLines().get(0).getLogContent().length());
    }
}
