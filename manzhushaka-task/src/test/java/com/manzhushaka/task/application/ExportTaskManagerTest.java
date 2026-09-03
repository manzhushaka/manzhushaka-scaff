package com.manzhushaka.task.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.context.SecurityContextHolder;

import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import com.manzhushaka.task.infrastructure.persistence.entity.ExportTask;
import com.manzhushaka.task.mapper.ExportTaskMapper;

import tools.jackson.databind.ObjectMapper;

/** 导出任务租约副作用测试。 */
class ExportTaskManagerTest
{
    @TempDir
    Path tempDir;

    @Test
    void staleWorkerShouldNotDeleteFileWhenFailureUpdateLosesLease() throws Exception
    {
        ExportTaskMapper mapper = mock(ExportTaskMapper.class);
        TaskHandler handler = mock(TaskHandler.class);
        TaskFileStorage storage = mock(TaskFileStorage.class);
        SecuritySnapshotService snapshotService = mock(SecuritySnapshotService.class);
        ExportTask task = new ExportTask();
        task.setTaskId(7L);
        task.setHandlerType("TEST_EXPORT");
        task.setFileKey("export/test.xlsx");
        task.setSecuritySnapshot("{}");
        Path file = tempDir.resolve("test.xlsx");
        Files.createFile(file);

        when(mapper.claim(anyLong(), any(), any(), anyString())).thenReturn(1);
        when(mapper.selectById(7L)).thenReturn(task);
        when(mapper.markFailure(anyLong(), anyString(), anyString(), any())).thenReturn(0);
        when(handler.handlerType()).thenReturn("TEST_EXPORT");
        when(storage.resolve(task.getFileKey())).thenReturn(file);
        when(snapshotService.install("{}")).thenReturn(SecurityContextHolder.createEmptyContext());
        org.mockito.Mockito.doThrow(new IllegalStateException("failed")).when(handler).execute(any());
        TaskHandlerRegistry registry = new TaskHandlerRegistry(List.of(handler));
        ExportTaskManager manager = new ExportTaskManager(mapper, registry, storage, snapshotService,
                mock(RedisStreamMessagePublisher.class), new ObjectMapper());

        manager.execute(7L);

        verify(storage, never()).deleteQuietly(task.getFileKey());
    }
}
