package com.manzhushaka.quartz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.manzhushaka.quartz.domain.SysJobLog;
import com.manzhushaka.quartz.domain.SysJobLogDetail;
import com.manzhushaka.quartz.mapper.SysJobLogDetailMapper;
import com.manzhushaka.quartz.mapper.SysJobLogMapper;
import com.manzhushaka.quartz.service.impl.SysJobLogServiceImpl;

/**
 * 定时任务调度日志服务测试。
 *
 * @author manzhushaka
 * @date 2026-06-30
 */
class SysJobLogServiceTest
{
    @Test
    void addJobLogShouldReturnGeneratedLog()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);
        SysJobLog jobLog = new SysJobLog();
        org.mockito.Mockito.doAnswer(invocation -> {
            SysJobLog value = invocation.getArgument(0);
            value.setJobLogId(100L);
            return 1;
        }).when(jobLogMapper).insertJobLog(jobLog);

        SysJobLog result = service.addJobLog(jobLog);

        assertThat(result.getJobLogId()).isEqualTo(100L);
    }

    @Test
    void addJobLogDetailsShouldIgnoreEmptyList()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);

        service.addJobLogDetails(Arrays.asList());

        org.mockito.Mockito.verifyNoInteractions(detailMapper);
    }

    @Test
    void addJobLogDetailsShouldDelegateMapper()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);
        SysJobLogDetail detail = new SysJobLogDetail();
        detail.setJobLogId(100L);
        detail.setLogLevel("INFO");
        detail.setLogContent("开始执行");
        detail.setSortNo(1);
        List<SysJobLogDetail> details = Arrays.asList(detail);

        service.addJobLogDetails(details);

        verify(detailMapper).insertJobLogDetails(details);
    }

    @Test
    void selectJobLogDetailListShouldReturnMapperResult()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);
        SysJobLogDetail detail = new SysJobLogDetail();
        detail.setDetailId(1L);
        when(detailMapper.selectJobLogDetailListByJobLogId(100L)).thenReturn(Arrays.asList(detail));

        List<SysJobLogDetail> result = service.selectJobLogDetailListByJobLogId(100L);

        assertThat(result).containsExactly(detail);
    }

    @Test
    void deleteJobLogByIdsShouldDeleteDetailsBeforeLogs()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);
        Long[] logIds = new Long[] { 100L, 101L };
        when(jobLogMapper.deleteJobLogByIds(logIds)).thenReturn(2);

        int result = service.deleteJobLogByIds(logIds);

        assertThat(result).isEqualTo(2);
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(detailMapper, jobLogMapper);
        inOrder.verify(detailMapper).deleteJobLogDetailByJobLogIds(logIds);
        inOrder.verify(jobLogMapper).deleteJobLogByIds(logIds);
    }

    @Test
    void deleteJobLogByIdShouldDeleteDetailsBeforeLog()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);
        when(jobLogMapper.deleteJobLogById(100L)).thenReturn(1);

        int result = service.deleteJobLogById(100L);

        assertThat(result).isEqualTo(1);
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(detailMapper, jobLogMapper);
        inOrder.verify(detailMapper).deleteJobLogDetailByJobLogId(100L);
        inOrder.verify(jobLogMapper).deleteJobLogById(100L);
    }

    @Test
    void cleanJobLogShouldCleanDetailsBeforeLogs()
    {
        SysJobLogMapper jobLogMapper = mock(SysJobLogMapper.class);
        SysJobLogDetailMapper detailMapper = mock(SysJobLogDetailMapper.class);
        SysJobLogServiceImpl service = createService(jobLogMapper, detailMapper);

        service.cleanJobLog();

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(detailMapper, jobLogMapper);
        inOrder.verify(detailMapper).cleanJobLogDetail();
        inOrder.verify(jobLogMapper).cleanJobLog();
    }

    /**
     * 创建待测服务。
     *
     * @param jobLogMapper 调度日志 Mapper
     * @param detailMapper 调度日志明细 Mapper
     * @return 待测服务
     */
    private SysJobLogServiceImpl createService(SysJobLogMapper jobLogMapper, SysJobLogDetailMapper detailMapper)
    {
        SysJobLogServiceImpl service = new SysJobLogServiceImpl();
        ReflectionTestUtils.setField(service, "jobLogMapper", jobLogMapper);
        ReflectionTestUtils.setField(service, "jobLogDetailMapper", detailMapper);
        return service;
    }
}
