package com.manzhushaka.framework.monitor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationLogBufferTest {

    /**
     * 验证日志缓冲区只保留最新的固定数量日志。
     */
    @Test
    void shouldKeepLatestLinesWithinCapacity() {
        ApplicationLogBuffer buffer = new ApplicationLogBuffer(2);

        buffer.append(LocalDateTime.of(2026, 6, 11, 10, 0, 0), "INFO first line");
        buffer.append(LocalDateTime.of(2026, 6, 11, 10, 0, 1), "WARN second line");
        buffer.append(LocalDateTime.of(2026, 6, 11, 10, 0, 2), "ERROR third line");

        assertEquals(List.of("WARN second line", "ERROR third line"), buffer.tailLines(10));
        assertEquals(2, buffer.size());
        assertEquals(LocalDateTime.of(2026, 6, 11, 10, 0, 2), buffer.lastEntryAt());
    }
}
