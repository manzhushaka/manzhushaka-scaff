package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportExportTaskTemplateTest {

    @Test
    void businessModuleShouldBeAbleToSubmitAndExecuteTypedExportTask() {
        SysImportExportTaskMapper taskMapper = mock(SysImportExportTaskMapper.class);
        ObjectStorageService storageService = mock(ObjectStorageService.class);
        ImportExportTaskAsyncExecutor asyncExecutor = mock(ImportExportTaskAsyncExecutor.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AtomicReference<SysImportExportTask> persistedTask = new AtomicReference<>();
        DemoExportTemplate template = new DemoExportTemplate(taskMapper, storageService, objectMapper);
        ImportExportTaskManager taskManager = new ImportExportTaskManager(asyncExecutor);
        LoginUser operator = new LoginUser();
        operator.setUsername("admin");

        doAnswer(invocation -> {
            SysImportExportTask entity = invocation.getArgument(0);
            entity.setId(101L);
            persistedTask.set(copyTask(entity));
            return 1;
        }).when(taskMapper).insert(any(SysImportExportTask.class));
        doAnswer(invocation -> {
            persistedTask.set(copyTask(invocation.getArgument(0)));
            return 1;
        }).when(taskMapper).updateById(any(SysImportExportTask.class));
        when(taskMapper.selectById(101L)).thenAnswer(invocation -> copyTask(persistedTask.get()));

        Long taskId = taskManager.submitExportTask(template, new DemoExportCommand("用户按部门导出", 9L), operator);
        template.execute(taskId);

        assertEquals(101L, taskId);
        verify(taskMapper).insert(argThat((SysImportExportTask entity) ->
            "EXPORT".equals(entity.getTaskType())
                && "SYS_USER_EXPORT".equals(entity.getBizType())
                && "用户按部门导出".equals(entity.getTaskName())
                && entity.getTaskParam() != null
                && entity.getTaskParam().contains("\"deptId\":9")
        ));
        verify(asyncExecutor).dispatch("EXPORT", "SYS_USER_EXPORT", 101L);
        verify(storageService).putObject(
            contains("/export/"),
            argThat((byte[] bytes) -> Arrays.equals(bytes, "deptId,username\n9,admin\n".getBytes(StandardCharsets.UTF_8))),
            eq("text/csv")
        );
        verify(taskMapper, atLeastOnce()).updateById(argThat((SysImportExportTask entity) ->
            "SUCCESS".equals(entity.getTaskStatus())
                && "dept-9-users.csv".equals(entity.getResultFileName())
                && Integer.valueOf(1).equals(entity.getTotalCount())
                && Integer.valueOf(1).equals(entity.getSuccessCount())
                && Integer.valueOf(0).equals(entity.getFailCount())
        ));
    }

    @Test
    void businessModuleShouldBeAbleToSubmitAndExecuteTypedImportTask() {
        SysImportExportTaskMapper taskMapper = mock(SysImportExportTaskMapper.class);
        ObjectStorageService storageService = mock(ObjectStorageService.class);
        ImportExportTaskAsyncExecutor asyncExecutor = mock(ImportExportTaskAsyncExecutor.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AtomicReference<SysImportExportTask> persistedTask = new AtomicReference<>();
        DemoImportTemplate template = new DemoImportTemplate(taskMapper, storageService, objectMapper);
        ImportExportTaskManager taskManager = new ImportExportTaskManager(asyncExecutor);
        LoginUser operator = new LoginUser();
        operator.setUsername("admin");

        doAnswer(invocation -> {
            SysImportExportTask entity = invocation.getArgument(0);
            entity.setId(202L);
            persistedTask.set(copyTask(entity));
            return 1;
        }).when(taskMapper).insert(any(SysImportExportTask.class));
        doAnswer(invocation -> {
            persistedTask.set(copyTask(invocation.getArgument(0)));
            return 1;
        }).when(taskMapper).updateById(any(SysImportExportTask.class));
        when(taskMapper.selectById(202L)).thenAnswer(invocation -> copyTask(persistedTask.get()));

        Long taskId = taskManager.submitImportTask(
            template,
            new DemoImportCommand(
                "VIP 用户导入",
                "VIP",
                "users.csv",
                "text/csv",
                "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8)
            ),
            operator
        );
        SysImportExportTask submittedTask = persistedTask.get();
        assertTrue(submittedTask.getSourceObjectKey().contains("/source/users.csv"));
        when(storageService.getObjectContent(submittedTask.getSourceObjectKey()))
            .thenReturn("username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8));

        template.execute(taskId);

        assertEquals(202L, taskId);
        verify(taskMapper).insert(argThat((SysImportExportTask entity) ->
            "IMPORT".equals(entity.getTaskType())
                && "CRM_CUSTOMER_IMPORT".equals(entity.getBizType())
                && "VIP 用户导入".equals(entity.getTaskName())
                && entity.getTaskParam() != null
                && entity.getTaskParam().contains("\"customerGroup\":\"VIP\"")
        ));
        verify(asyncExecutor).dispatch("IMPORT", "CRM_CUSTOMER_IMPORT", 202L);
        verify(storageService).putObject(
            contains("/source/"),
            argThat((byte[] bytes) -> Arrays.equals(bytes, "username,nickname\nadmin,系统管理员\n".getBytes(StandardCharsets.UTF_8))),
            eq("text/csv")
        );
        verify(storageService).putObject(
            contains("/result/"),
            argThat((byte[] bytes) -> Arrays.equals(
                bytes,
                "rowNumber,group,username,result\n2,VIP,admin,SUCCESS\n".getBytes(StandardCharsets.UTF_8)
            )),
            eq("text/csv")
        );
        verify(taskMapper, atLeastOnce()).updateById(argThat((SysImportExportTask entity) ->
            "SUCCESS".equals(entity.getTaskStatus())
                && "vip-customers-report.csv".equals(entity.getResultFileName())
                && Integer.valueOf(1).equals(entity.getTotalCount())
                && Integer.valueOf(1).equals(entity.getSuccessCount())
                && Integer.valueOf(0).equals(entity.getFailCount())
        ));
    }

    @Test
    void importTaskShouldRejectOversizedOrUnsupportedFiles() {
        SysImportExportTaskMapper taskMapper = mock(SysImportExportTaskMapper.class);
        ObjectStorageService storageService = mock(ObjectStorageService.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        DemoImportTemplate template = new DemoImportTemplate(taskMapper, storageService, objectMapper);

        DemoImportCommand unsupportedType = new DemoImportCommand(
            "VIP 用户导入",
            "VIP",
            "users.exe",
            "application/octet-stream",
            "x".getBytes(StandardCharsets.UTF_8)
        );
        BizException unsupportedTypeException = assertThrows(BizException.class, () -> template.submit(unsupportedType, null));
        assertEquals("仅支持上传 csv、xls、xlsx 格式文件", unsupportedTypeException.getMessage());

        DemoImportCommand oversized = new DemoImportCommand(
            "VIP 用户导入",
            "VIP",
            "users.csv",
            "text/csv",
            new byte[51 * 1024 * 1024]
        );
        BizException oversizedException = assertThrows(BizException.class, () -> template.submit(oversized, null));
        assertEquals("导入文件大小不能超过 50MB", oversizedException.getMessage());
    }

    private static SysImportExportTask copyTask(SysImportExportTask source) {
        if (source == null) {
            return null;
        }
        SysImportExportTask target = new SysImportExportTask();
        target.setId(source.getId());
        target.setTaskNo(source.getTaskNo());
        target.setTaskType(source.getTaskType());
        target.setBizType(source.getBizType());
        target.setBizLabel(source.getBizLabel());
        target.setTaskName(source.getTaskName());
        target.setTaskStatus(source.getTaskStatus());
        target.setTaskMessage(source.getTaskMessage());
        target.setTaskParam(source.getTaskParam());
        target.setSourceFileName(source.getSourceFileName());
        target.setSourceObjectKey(source.getSourceObjectKey());
        target.setSourceFileSize(source.getSourceFileSize());
        target.setResultFileName(source.getResultFileName());
        target.setResultObjectKey(source.getResultObjectKey());
        target.setResultFileSize(source.getResultFileSize());
        target.setTotalCount(source.getTotalCount());
        target.setSuccessCount(source.getSuccessCount());
        target.setFailCount(source.getFailCount());
        target.setCreateBy(source.getCreateBy());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setFinishedTime(source.getFinishedTime());
        return target;
    }

    private static final class DemoExportTemplate extends AbstractExportTaskTemplate<DemoExportCommand> {

        private DemoExportTemplate(
            SysImportExportTaskMapper taskMapper,
            ObjectStorageService storageService,
            ObjectMapper objectMapper
        ) {
            super(taskMapper, storageService, objectMapper, DemoExportCommand.class);
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
        protected TaskExecutionResult executeExport(SysImportExportTask task, DemoExportCommand command) {
            return TaskExecutionResult.success(
                1,
                1,
                0,
                "导出成功",
                new TaskFileArtifact(
                    "dept-" + command.getDeptId() + "-users.csv",
                    "text/csv",
                    ("deptId,username\n" + command.getDeptId() + ",admin\n").getBytes(StandardCharsets.UTF_8)
                )
            );
        }
    }

    private static final class DemoImportTemplate extends AbstractImportTaskTemplate<DemoImportCommand> {

        private DemoImportTemplate(
            SysImportExportTaskMapper taskMapper,
            ObjectStorageService storageService,
            ObjectMapper objectMapper
        ) {
            super(taskMapper, storageService, objectMapper, DemoImportCommand.class);
        }

        @Override
        public String bizType() {
            return "CRM_CUSTOMER_IMPORT";
        }

        @Override
        public String bizLabel() {
            return "客户导入";
        }

        @Override
        protected String defaultTaskName() {
            return "客户导入";
        }

        @Override
        protected void validateSubmit(DemoImportCommand command) {
            super.validateSubmit(command);
        }

        @Override
        protected TaskExecutionResult executeImport(
            SysImportExportTask task,
            DemoImportCommand command,
            TaskSourceFile sourceFile
        ) {
            assertEquals("users.csv", sourceFile.fileName());
            return TaskExecutionResult.success(
                1,
                1,
                0,
                "导入成功",
                new TaskFileArtifact(
                    "vip-customers-report.csv",
                    "text/csv",
                    ("rowNumber,group,username,result\n2," + command.getCustomerGroup() + ",admin,SUCCESS\n")
                        .getBytes(StandardCharsets.UTF_8)
                )
            );
        }
    }

    private static final class DemoExportCommand extends ExportTaskSubmitCommand {
        private Long deptId;

        @JsonCreator
        private DemoExportCommand(
            @JsonProperty("taskName") String taskName,
            @JsonProperty("deptId") Long deptId
        ) {
            setTaskName(taskName);
            this.deptId = deptId;
        }

        public Long getDeptId() {
            return deptId;
        }
    }

    private static final class DemoImportCommand extends ImportTaskSubmitCommand {
        private String customerGroup;

        @JsonCreator
        private DemoImportCommand(
            @JsonProperty("taskName") String taskName,
            @JsonProperty("customerGroup") String customerGroup,
            @JsonProperty("fileName") String fileName,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("content") byte[] content
        ) {
            setTaskName(taskName);
            this.customerGroup = customerGroup;
            setFileName(fileName);
            setContentType(contentType);
            setContent(content);
        }

        public String getCustomerGroup() {
            return customerGroup;
        }
    }
}
