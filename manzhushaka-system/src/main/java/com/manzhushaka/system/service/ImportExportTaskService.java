package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.impexp.ImportExportTaskQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.impexp.DownloadUrlVO;
import com.manzhushaka.system.vo.impexp.ImportExportTaskVO;

import java.util.List;

/**
 * 定义 ImportExportTaskService 服务能力。
 */
public interface ImportExportTaskService {

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<ImportExportTaskVO> page(ImportExportTaskQuery query);

    /**
     * 执行 scene Options 逻辑。
     *
     * @param taskType taskType 参数
     * @return 处理结果
     */
    List<LabelValueOption> sceneOptions(String taskType);

    /**
     * 生成下载地址。
     *
     * @param id 主键 ID
     * @param fileRole fileRole 参数
     * @return 创建结果
     */
    DownloadUrlVO generateDownloadUrl(Long id, String fileRole);
}
