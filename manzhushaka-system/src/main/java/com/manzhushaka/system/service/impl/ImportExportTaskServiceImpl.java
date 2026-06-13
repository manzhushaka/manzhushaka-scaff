package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.ObjectStorageService;
import com.manzhushaka.system.dto.impexp.ImportExportTaskQuery;
import com.manzhushaka.system.service.ImportExportTaskService;
import com.manzhushaka.system.service.impexp.ExportTaskTemplateRegistry;
import com.manzhushaka.system.service.impexp.ImportExportTaskSupport;
import com.manzhushaka.system.service.impexp.ImportTaskTemplateRegistry;
import com.manzhushaka.system.service.support.SystemAccessSupport;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.impexp.DownloadUrlVO;
import com.manzhushaka.system.vo.impexp.ImportExportTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 实现 ImportExportTaskServiceImpl 业务服务。
 */
@Service
public class ImportExportTaskServiceImpl implements ImportExportTaskService {

    private final SysImportExportTaskMapper taskMapper;
    private final ExportTaskTemplateRegistry exportTaskTemplateRegistry;
    private final ImportTaskTemplateRegistry importTaskTemplateRegistry;
    private final ObjectStorageService storageService;
    private final SystemAccessSupport accessSupport;

    public ImportExportTaskServiceImpl(
        SysImportExportTaskMapper taskMapper,
        ExportTaskTemplateRegistry exportTaskTemplateRegistry,
        ImportTaskTemplateRegistry importTaskTemplateRegistry,
        ObjectStorageService storageService,
        SystemAccessSupport accessSupport
    ) {
        this.taskMapper = taskMapper;
        this.exportTaskTemplateRegistry = exportTaskTemplateRegistry;
        this.importTaskTemplateRegistry = importTaskTemplateRegistry;
        this.storageService = storageService;
        this.accessSupport = accessSupport;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @Override
    public PageResult<ImportExportTaskVO> page(ImportExportTaskQuery query) {
        LambdaQueryWrapper<SysImportExportTask> wrapper = new LambdaQueryWrapper<SysImportExportTask>()
            .eq(StringUtils.hasText(query.getTaskType()), SysImportExportTask::getTaskType, query.getTaskType())
            .eq(StringUtils.hasText(query.getBizType()), SysImportExportTask::getBizType, query.getBizType())
            .like(StringUtils.hasText(query.getTaskName()), SysImportExportTask::getTaskName, query.getTaskName())
            .eq(StringUtils.hasText(query.getTaskStatus()), SysImportExportTask::getTaskStatus, query.getTaskStatus())
            .orderByDesc(SysImportExportTask::getId);
        applyTaskVisibility(wrapper);
        Page<SysImportExportTask> page = taskMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toVO);
    }

    /**
     * 执行 scene Options 逻辑。
     *
     * @param taskType taskType 参数
     * @return 处理结果
     */
    @Override
    public List<LabelValueOption> sceneOptions(String taskType) {
        if (ImportExportTaskSupport.TASK_TYPE_EXPORT.equals(taskType)) {
            return exportTaskTemplateRegistry.options();
        }
        if (ImportExportTaskSupport.TASK_TYPE_IMPORT.equals(taskType)) {
            return importTaskTemplateRegistry.options();
        }
        throw new BizException(400, "不支持的任务类型");
    }

    /**
     * 生成下载地址。
     *
     * @param id 主键 ID
     * @param fileRole fileRole 参数
     * @return 创建结果
     */
    @Override
    public DownloadUrlVO generateDownloadUrl(Long id, String fileRole) {
        SysImportExportTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(404, "任务不存在");
        }
        accessSupport.assertTaskAccessible(task);
        if (ImportExportTaskSupport.FILE_ROLE_SOURCE.equals(fileRole)) {
            if (!StringUtils.hasText(task.getSourceObjectKey())) {
                throw new BizException(400, "当前任务没有源文件可下载");
            }
            return new DownloadUrlVO(storageService.generateDownloadUrl(task.getSourceObjectKey(), task.getSourceFileName()));
        }
        if (ImportExportTaskSupport.FILE_ROLE_RESULT.equals(fileRole)) {
            if (!StringUtils.hasText(task.getResultObjectKey())) {
                throw new BizException(400, "当前任务没有结果文件可下载");
            }
            return new DownloadUrlVO(storageService.generateDownloadUrl(task.getResultObjectKey(), task.getResultFileName()));
        }
        throw new BizException(400, "不支持的文件类型");
    }

    /**
     * 按当前用户身份收敛导入导出任务的可见范围。
     *
     * @param wrapper 任务查询条件
     */
    private void applyTaskVisibility(LambdaQueryWrapper<SysImportExportTask> wrapper) {
        if (accessSupport.hasAllDataScope()) {
            return;
        }
        wrapper.eq(SysImportExportTask::getCreateBy, accessSupport.requireCurrentUser().getUsername());
    }

    /**
     * 构建 to VO 结果。
     *
     * @param task task 参数
     * @return 处理结果
     */
    private ImportExportTaskVO toVO(SysImportExportTask task) {
        ImportExportTaskVO vo = new ImportExportTaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setTaskType(task.getTaskType());
        vo.setBizType(task.getBizType());
        vo.setBizLabel(task.getBizLabel());
        vo.setTaskName(task.getTaskName());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setTaskMessage(task.getTaskMessage());
        vo.setSourceFileName(task.getSourceFileName());
        vo.setResultFileName(task.getResultFileName());
        vo.setTotalCount(task.getTotalCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setFailCount(task.getFailCount());
        vo.setCreateBy(task.getCreateBy());
        vo.setCreateTime(task.getCreateTime());
        vo.setFinishedTime(task.getFinishedTime());
        return vo;
    }
}
