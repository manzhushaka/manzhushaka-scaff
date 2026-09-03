package com.manzhushaka.task.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.manzhushaka.common.config.ManzhushakaConfig;

/** 任务私有文件路径测试。 */
class TaskFileStorageTest
{
    private String originalProfile;

    @BeforeEach
    void setUp()
    {
        originalProfile = ManzhushakaConfig.getProfile();
        new ManzhushakaConfig().setProfile("target/task-storage-test");
    }

    @AfterEach
    void tearDown()
    {
        new ManzhushakaConfig().setProfile(originalProfile);
    }

    @Test
    void shouldResolveFileInsidePrivateRoot()
    {
        Path path = new TaskFileStorage().resolve("export/result.xlsx");

        assertTrue(path.endsWith("async-task/export/result.xlsx"));
    }

    @Test
    void shouldRejectTraversalFileKey()
    {
        TaskFileStorage storage = new TaskFileStorage();

        assertThrows(IllegalArgumentException.class, () -> storage.resolve("../secret.xlsx"));
        assertThrows(IllegalArgumentException.class, () -> storage.resolve("/absolute.xlsx"));
    }
}
