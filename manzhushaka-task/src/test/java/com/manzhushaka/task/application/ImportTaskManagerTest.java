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
import com.manzhushaka.task.infrastructure.persistence.entity.ImportTask;
import com.manzhushaka.task.mapper.ImportTaskMapper;

import tools.jackson.databind.ObjectMapper;

/** 导入任务租约和文件清理测试。 */
class ImportTaskManagerTest
{
    @TempDir
    Path tempDir;

    @Test
    void staleWorkerShouldNotDeleteInputWhenFailureUpdateLosesLease() throws Exception
    {
        TestFixture fixture = fixture();
        org.mockito.Mockito.doThrow(new IllegalStateException("failed"))
                .when(fixture.handler).execute(any());
        when(fixture.mapper.markFailure(anyLong(), anyString(), anyString(), any())).thenReturn(0);

        fixture.manager.execute(fixture.task.getTaskId());

        verify(fixture.storage, never()).deleteQuietly(fixture.task.getFileKey());
    }

    @Test
    void successfulOwnerShouldMarkSuccessAndDeleteInput() throws Exception
    {
        TestFixture fixture = fixture();
        fixture.task.setFailureCount(0L);
        when(fixture.mapper.markSuccess(anyLong(), anyString(), anyString(), any())).thenReturn(1);

        fixture.manager.execute(fixture.task.getTaskId());

        verify(fixture.mapper).markSuccess(anyLong(), anyString(), anyString(), any());
        verify(fixture.storage).deleteQuietly(fixture.task.getFileKey());
    }

    @Test
    void cancelledOwnerShouldMarkCancelledAndDeleteInput() throws Exception
    {
        TestFixture fixture = fixture();
        org.mockito.Mockito.doThrow(new TaskCancelledException()).when(fixture.handler).execute(any());
        when(fixture.mapper.markCancelled(anyLong(), anyString(), any())).thenReturn(1);

        fixture.manager.execute(fixture.task.getTaskId());

        verify(fixture.mapper).markCancelled(anyLong(), anyString(), any());
        verify(fixture.storage).deleteQuietly(fixture.task.getFileKey());
    }

    private TestFixture fixture() throws Exception
    {
        ImportTaskMapper mapper = mock(ImportTaskMapper.class);
        TaskHandler handler = mock(TaskHandler.class);
        TaskFileStorage storage = mock(TaskFileStorage.class);
        SecuritySnapshotService snapshotService = mock(SecuritySnapshotService.class);
        ImportTask task = new ImportTask();
        task.setTaskId(9L);
        task.setHandlerType("TEST_IMPORT");
        task.setFileKey("import/test.xlsx");
        task.setSecuritySnapshot("{}");
        Path file = tempDir.resolve("test.xlsx");
        Files.createFile(file);

        when(mapper.claim(anyLong(), any(), any(), anyString())).thenReturn(1);
        when(mapper.selectById(task.getTaskId())).thenReturn(task);
        when(handler.handlerType()).thenReturn(task.getHandlerType());
        when(storage.resolve(task.getFileKey())).thenReturn(file);
        when(snapshotService.install("{}")).thenReturn(SecurityContextHolder.createEmptyContext());
        TaskHandlerRegistry registry = new TaskHandlerRegistry(List.of(handler));
        ImportTaskManager manager = new ImportTaskManager(mapper, registry, storage, snapshotService,
                mock(RedisStreamMessagePublisher.class), new ObjectMapper());
        return new TestFixture(mapper, handler, storage, task, manager);
    }

    private record TestFixture(ImportTaskMapper mapper, TaskHandler handler, TaskFileStorage storage,
            ImportTask task, ImportTaskManager manager)
    {
    }
}
