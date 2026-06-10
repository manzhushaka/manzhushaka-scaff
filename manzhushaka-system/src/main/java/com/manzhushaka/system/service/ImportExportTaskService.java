package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.impexp.ExportTaskCreateForm;
import com.manzhushaka.system.dto.impexp.ImportExportTaskQuery;
import com.manzhushaka.system.dto.impexp.ImportTaskCreateCommand;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.impexp.DownloadUrlVO;
import com.manzhushaka.system.vo.impexp.ImportExportTaskVO;

import java.util.List;

public interface ImportExportTaskService {

    PageResult<ImportExportTaskVO> page(ImportExportTaskQuery query);

    List<LabelValueOption> sceneOptions(String taskType);

    Long createExportTask(ExportTaskCreateForm form);

    Long createImportTask(ImportTaskCreateCommand command);

    DownloadUrlVO generateDownloadUrl(Long id, String fileRole);
}
