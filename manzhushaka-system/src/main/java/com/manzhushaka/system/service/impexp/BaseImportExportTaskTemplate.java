package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 定义 BaseImportExportTaskTemplate。
 */
abstract class BaseImportExportTaskTemplate {

    protected final SysImportExportTaskMapper taskMapper;
    protected final ObjectStorageService storageService;
    private final ObjectMapper objectMapper;
    private final String storageBasePath;

    protected BaseImportExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        ObjectMapper objectMapper
    ) {
        this(taskMapper, storageService, objectMapper, "import-export");
    }

    protected BaseImportExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        ObjectMapper objectMapper,
        String storageBasePath
    ) {
        this.taskMapper = taskMapper;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.storageBasePath = StringUtils.hasText(storageBasePath) ? trimSlashes(storageBasePath) : "import-export";
    }

    /**
     * 执行 biz Type 逻辑。
     *
     * @return 处理结果
     */
    public abstract String bizType();

    /**
     * 执行 biz Label 逻辑。
     *
     * @return 处理结果
     */
    public abstract String bizLabel();

    /**
     * 执行 default Task Name 逻辑。
     *
     * @return 处理结果
     */
    protected abstract String defaultTaskName();

    /**
     * 构建 build Source Object Key 结果。
     *
     * @param taskNo taskNo 参数
     * @param fileName fileName 参数
     * @return 处理结果
     */
    protected String buildSourceObjectKey(String taskNo, String fileName) {
        return storageBasePath + "/import/" + taskNo + "/source/" + normalizeFileName(fileName);
    }

    /**
     * 构建 build Result Object Key 结果。
     *
     * @param taskType taskType 参数
     * @param taskNo taskNo 参数
     * @param fileName fileName 参数
     * @return 处理结果
     */
    protected String buildResultObjectKey(String taskType, String taskNo, String fileName) {
        return storageBasePath + "/" + taskType.toLowerCase() + "/" + taskNo + "/result/" + normalizeFileName(fileName);
    }

    /**
     * 更新 apply Operator 数据。
     *
     * @param task task 参数
     * @param operator operator 参数
     */
    protected void applyOperator(SysImportExportTask task, LoginUser operator) {
        String username = operator == null || !StringUtils.hasText(operator.getUsername()) ? "system" : operator.getUsername().trim();
        task.setCreateBy(username);
        task.setUpdateBy(username);
    }

    /**
     * 查询 load Task 结果。
     *
     * @param taskId 任务 ID
     * @return 查询结果
     */
    protected SysImportExportTask loadTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    /**
     * 更新 mark Processing 数据。
     *
     * @param task task 参数
     */
    protected void markProcessing(SysImportExportTask task) {
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_PROCESSING);
        task.setTaskMessage("任务处理中");
        taskMapper.updateById(task);
    }

    /**
     * 更新 mark Success 数据。
     *
     * @param task task 参数
     * @param result result 参数
     */
    protected void markSuccess(SysImportExportTask task, TaskExecutionResult result) {
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_SUCCESS);
        task.setTaskMessage(result.message());
        task.setTotalCount(result.totalCount());
        task.setSuccessCount(result.successCount());
        task.setFailCount(result.failCount());
        task.setFinishedTime(LocalDateTime.now());
        TaskFileArtifact resultFile = result.resultFile();
        if (resultFile != null) {
            String resultObjectKey = buildResultObjectKey(task.getTaskType(), task.getTaskNo(), resultFile.fileName());
            storageService.putObject(resultObjectKey, resultFile.content(), resultFile.contentType());
            task.setResultFileName(resultFile.fileName());
            task.setResultObjectKey(resultObjectKey);
            task.setResultFileSize((long) resultFile.content().length);
        }
        taskMapper.updateById(task);
    }

    /**
     * 更新 mark Fail 数据。
     *
     * @param task task 参数
     * @param exception 异常对象
     */
    protected void markFail(SysImportExportTask task, Exception exception) {
        task.setTaskStatus(ImportExportTaskSupport.TASK_STATUS_FAIL);
        task.setTaskMessage(limitMessage(exception.getMessage()));
        task.setFinishedTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    /**
     * 校验 ensure Biz Type 条件。
     *
     * @param requestBizType requestBizType 参数
     */
    protected void ensureBizType(String requestBizType) {
        if (!bizType().equals(requestBizType)) {
            throw new IllegalArgumentException("任务场景不匹配");
        }
    }

    /**
     * 更新 write Task Param 数据。
     *
     * @param value 字段值
     * @return 处理结果
     */
    protected String writeTaskParam(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(500, "任务参数序列化失败");
        }
    }

    /**
     * 执行 read Task Param 逻辑。
     *
     * @param taskParam taskParam 参数
     * @param valueType valueType 参数
     * @return 处理结果
     */
    protected <T> T readTaskParam(String taskParam, Class<T> valueType) {
        if (!StringUtils.hasText(taskParam) || valueType == null) {
            return null;
        }
        try {
            return objectMapper.readValue(taskParam, valueType);
        } catch (JsonProcessingException exception) {
            throw new BizException(500, "任务参数解析失败");
        }
    }

    /**
     * 构建 normalize File Name 结果。
     *
     * @param fileName fileName 参数
     * @return 处理结果
     */
    private String normalizeFileName(String fileName) {
        String normalized = StringUtils.hasText(fileName) ? fileName.trim() : "file.bin";
        normalized = normalized.replace('\\', '-').replace('/', '-');
        return normalized;
    }

    /**
     * 执行 limit Message 逻辑。
     *
     * @param message message 参数
     * @return 处理结果
     */
    private String limitMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "任务执行失败";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    /**
     * 执行 trim Slashes 逻辑。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private String trimSlashes(String value) {
        String normalized = value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
