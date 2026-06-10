package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import com.manzhushaka.system.dto.impexp.ExportTaskCreateForm;
import com.manzhushaka.system.dto.impexp.ImportTaskCreateCommand;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportExportTaskTemplateTest {

    @Test
    void shouldUploadGeneratedExportFileAndMarkTaskSuccess() {
        SysImportExportTaskMapper taskMapper = mock(SysImportExportTaskMapper.class);
        ObjectStorageService storageService = mock(ObjectStorageService.class);
        DemoExportTemplate template = new DemoExportTemplate(taskMapper, storageService);
        SysImportExportTask task = new SysImportExportTask();
        task.setId(101L);
        task.setTaskNo("EXP-101");
        task.setTaskType("EXPORT");
        task.setTaskStatus("PENDING");
        when(taskMapper.selectById(101L)).thenReturn(task);

        template.execute(101L);

        verify(storageService).putObject(
            contains("/export/"),
            argThat((byte[] bytes) -> Arrays.equals(bytes, "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8))),
            eq("text/csv")
        );
        verify(taskMapper, atLeastOnce()).updateById(argThat((SysImportExportTask entity) ->
            "SUCCESS".equals(entity.getTaskStatus())
                && "users.csv".equals(entity.getResultFileName())
                && entity.getResultObjectKey() != null
                && entity.getFinishedTime() != null
                && Integer.valueOf(1).equals(entity.getTotalCount())
                && Integer.valueOf(1).equals(entity.getSuccessCount())
                && Integer.valueOf(0).equals(entity.getFailCount())
        ));
    }

    @Test
    void shouldPersistImportSourceAndGenerateResultReport() {
        SysImportExportTaskMapper taskMapper = mock(SysImportExportTaskMapper.class);
        ObjectStorageService storageService = mock(ObjectStorageService.class);
        DemoImportTemplate template = new DemoImportTemplate(taskMapper, storageService);
        LoginUser operator = new LoginUser();
        operator.setUsername("admin");
        ImportTaskCreateCommand command = new ImportTaskCreateCommand(
            "SYS_USER_IMPORT",
            "系统用户导入",
            "users.csv",
            "text/csv",
            "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8)
        );

        doAnswer(invocation -> {
            SysImportExportTask entity = invocation.getArgument(0);
            entity.setId(202L);
            return 1;
        }).when(taskMapper).insert(any(SysImportExportTask.class));
        when(taskMapper.selectById(202L)).thenAnswer(invocation -> {
            SysImportExportTask task = new SysImportExportTask();
            task.setId(202L);
            task.setTaskNo("IMP-202");
            task.setTaskType("IMPORT");
            task.setTaskStatus("PENDING");
            task.setSourceFileName("users.csv");
            task.setSourceObjectKey("import-export/import/IMP-202/source/users.csv");
            return task;
        });
        when(storageService.getObjectContent("import-export/import/IMP-202/source/users.csv"))
            .thenReturn("username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8));

        Long taskId = template.submit(command, operator);
        template.execute(taskId);

        assertEquals(202L, taskId);
        verify(storageService).putObject(
            contains("/source/"),
            argThat((byte[] bytes) -> Arrays.equals(bytes, "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8))),
            eq("text/csv")
        );
        verify(storageService).putObject(
            contains("/result/"),
            argThat((byte[] bytes) -> Arrays.equals(bytes, "rowNumber,username,result,message\n2,admin,SUCCESS,校验通过\n".getBytes(StandardCharsets.UTF_8))),
            eq("text/csv")
        );
        verify(taskMapper, atLeastOnce()).updateById(argThat((SysImportExportTask entity) ->
            "SUCCESS".equals(entity.getTaskStatus())
                && "report.csv".equals(entity.getResultFileName())
                && entity.getFinishedTime() != null
                && Integer.valueOf(1).equals(entity.getTotalCount())
                && Integer.valueOf(1).equals(entity.getSuccessCount())
                && Integer.valueOf(0).equals(entity.getFailCount())
        ));
    }

    private static final class DemoExportTemplate extends AbstractExportTaskTemplate {

        private DemoExportTemplate(SysImportExportTaskMapper taskMapper, ObjectStorageService storageService) {
            super(taskMapper, storageService);
        }

        @Override
        public String bizType() {
            return "SYS_USER_EXPORT";
        }

        @Override
        public String bizLabel() {
            return "系统用户导出";
        }

        @Override
        protected String defaultTaskName() {
            return "系统用户导出";
        }

        @Override
        protected TaskExecutionResult executeExport(SysImportExportTask task) {
            assertNotNull(task);
            return TaskExecutionResult.success(
                1,
                1,
                0,
                "导出成功",
                new TaskFileArtifact(
                    "users.csv",
                    "text/csv",
                    "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8)
                )
            );
        }
    }

    private static final class DemoImportTemplate extends AbstractImportTaskTemplate {

        private DemoImportTemplate(SysImportExportTaskMapper taskMapper, ObjectStorageService storageService) {
            super(taskMapper, storageService);
        }

        @Override
        public String bizType() {
            return "SYS_USER_IMPORT";
        }

        @Override
        public String bizLabel() {
            return "系统用户导入";
        }

        @Override
        protected String defaultTaskName() {
            return "系统用户导入";
        }

        @Override
        protected TaskExecutionResult executeImport(SysImportExportTask task, TaskSourceFile sourceFile) {
            assertNotNull(task);
            assertEquals("users.csv", sourceFile.fileName());
            return TaskExecutionResult.success(
                1,
                1,
                0,
                "导入校验完成",
                new TaskFileArtifact(
                    "report.csv",
                    "text/csv",
                    "rowNumber,username,result,message\n2,admin,SUCCESS,校验通过\n".getBytes(StandardCharsets.UTF_8)
                )
            );
        }
    }

    @SuppressWarnings("unused")
    private static ExportTaskCreateForm unusedFormReference() {
        return new ExportTaskCreateForm();
    }
}
