package com.manzhushaka.web.service;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.web.dto.monitor.RuntimeLogQuery;
import com.manzhushaka.web.vo.monitor.RuntimeLogLineVO;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 运行日志在线查看服务测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RuntimeLogServiceTest {

    /**
     * 只允许读取日志目录下的白名单文件。
     *
     * @throws Exception 创建临时文件失败
     */
    @Test
    void listShouldRejectPathTraversalFileName() throws Exception {
        RuntimeLogService service = new RuntimeLogService(tempLogDirectory());
        RuntimeLogQuery query = new RuntimeLogQuery();
        query.setFileName("../application.yml");

        assertThatThrownBy(() -> service.list(query))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("日志文件不允许访问");
    }

    /**
     * 错误日志应能把异常堆栈合并到同一条日志记录中，便于页面查看。
     *
     * @throws Exception 创建临时文件失败
     */
    @Test
    void listShouldAttachStackTraceToPreviousErrorLine() throws Exception {
        Path logDirectory = tempLogDirectory();
        Files.writeString(logDirectory.resolve("sys-error.log"),
                "12:00:00.001 [main] ERROR c.m.Test - [run,10] - failed\n"
                        + "java.lang.IllegalStateException: broken\n"
                        + "\tat com.manzhushaka.Test.run(Test.java:10)\n",
                StandardCharsets.UTF_8);
        RuntimeLogService service = new RuntimeLogService(logDirectory);
        RuntimeLogQuery query = new RuntimeLogQuery();
        query.setFileName("sys-error.log");
        query.setLineCount(100);

        List<RuntimeLogLineVO> lines = service.list(query);

        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getLevel()).isEqualTo("ERROR");
        assertThat(lines.get(0).getStackTraceBlock()).contains("IllegalStateException").contains("Test.run");
    }

    /**
     * 创建临时日志目录。
     *
     * @return 临时日志目录
     * @throws Exception 创建目录失败
     */
    private Path tempLogDirectory() throws Exception {
        Path logDirectory = Files.createTempDirectory("runtime-log-service-test");
        Files.writeString(logDirectory.resolve("sys-info.log"), "", StandardCharsets.UTF_8);
        Files.writeString(logDirectory.resolve("sys-warn.log"), "", StandardCharsets.UTF_8);
        Files.writeString(logDirectory.resolve("sys-error.log"), "", StandardCharsets.UTF_8);
        Files.writeString(logDirectory.resolve("sys-user.log"), "", StandardCharsets.UTF_8);
        return logDirectory;
    }
}
