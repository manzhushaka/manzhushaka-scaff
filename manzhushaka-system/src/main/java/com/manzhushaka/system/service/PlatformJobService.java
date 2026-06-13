package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.job.PlatformJobForm;
import com.manzhushaka.system.dto.job.PlatformJobLogQuery;
import com.manzhushaka.system.dto.job.PlatformJobQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.job.PlatformJobLogDetailVO;
import com.manzhushaka.system.vo.job.PlatformJobLogVO;
import com.manzhushaka.system.vo.job.PlatformJobVO;

import java.util.List;

/**
 * 定义 PlatformJobService 服务能力。
 */
public interface PlatformJobService {

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<PlatformJobVO> page(PlatformJobQuery query);

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    PlatformJobVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(PlatformJobForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, PlatformJobForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);

    /**
     * 暂停任务。
     *
     * @param id 主键 ID
     */
    void pause(Long id);

    /**
     * 恢复任务。
     *
     * @param id 主键 ID
     */
    void resume(Long id);

    /**
     * 触发任务执行。
     *
     * @param id 主键 ID
     */
    void trigger(Long id);

    /**
     * 处理 handler Options 流程。
     *
     * @return 处理结果
     */
    List<LabelValueOption> handlerOptions();

    /**
     * 查询 page Logs 结果。
     *
     * @param jobId jobId 标识
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<PlatformJobLogVO> pageLogs(Long jobId, PlatformJobLogQuery query);

    /**
     * 返回 logDetail。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    PlatformJobLogDetailVO getLogDetail(Long id);

    /**
     * 查询 list All Definitions 结果。
     *
     * @return 查询结果
     */
    List<com.manzhushaka.framework.job.PlatformJobDefinition> listAllDefinitions();
}
