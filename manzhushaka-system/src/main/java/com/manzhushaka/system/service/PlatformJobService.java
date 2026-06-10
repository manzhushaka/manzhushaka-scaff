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

public interface PlatformJobService {

    PageResult<PlatformJobVO> page(PlatformJobQuery query);

    PlatformJobVO getById(Long id);

    Long create(PlatformJobForm form);

    void update(Long id, PlatformJobForm form);

    void delete(Long id);

    void pause(Long id);

    void resume(Long id);

    void trigger(Long id);

    List<LabelValueOption> handlerOptions();

    PageResult<PlatformJobLogVO> pageLogs(Long jobId, PlatformJobLogQuery query);

    PlatformJobLogDetailVO getLogDetail(Long id);

    List<com.manzhushaka.framework.job.PlatformJobDefinition> listAllDefinitions();
}
