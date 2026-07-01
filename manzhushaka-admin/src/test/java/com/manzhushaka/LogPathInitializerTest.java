package com.manzhushaka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * LogPathInitializer 单元测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class LogPathInitializerTest {

    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("dev 环境日志写入当前工作目录 logs")
    void resolveLogPathWithDevProfileShouldUseWorkingDirectoryLogs() {
        Path userDir = tempDir.resolve("workspace");
        Path jarPath = tempDir.resolve("deploy").resolve("manzhushaka-admin.jar");

        Path logPath = LogPathInitializer.resolveLogPath("dev", jarPath, true, userDir);

        assertEquals(userDir.resolve("logs").toAbsolutePath().normalize(), logPath);
    }

    @Test
    @DisplayName("未显式指定环境时按本机开发处理")
    void resolveLogPathWithEmptyProfileShouldUseWorkingDirectoryLogs() {
        Path userDir = tempDir.resolve("workspace");
        Path jarPath = tempDir.resolve("deploy").resolve("manzhushaka-admin.jar");

        Path logPath = LogPathInitializer.resolveLogPath("", jarPath, true, userDir);

        assertEquals(userDir.resolve("logs").toAbsolutePath().normalize(), logPath);
    }

    @Test
    @DisplayName("非 dev 环境日志写入 jar 同级 logs")
    void resolveLogPathWithProdProfileShouldUseJarSiblingLogs() {
        Path userDir = tempDir.resolve("workspace");
        Path jarPath = tempDir.resolve("deploy").resolve("manzhushaka-admin.jar");

        Path logPath = LogPathInitializer.resolveLogPath("prod", jarPath, true, userDir);

        assertEquals(jarPath.getParent().resolve("logs").toAbsolutePath().normalize(), logPath);
    }
}
