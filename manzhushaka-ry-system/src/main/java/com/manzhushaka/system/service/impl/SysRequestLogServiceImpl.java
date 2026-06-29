package com.manzhushaka.system.service.impl;

import java.util.List;

import com.manzhushaka.system.domain.SysRequestLog;
import com.manzhushaka.system.mapper.SysRequestLogMapper;
import com.manzhushaka.system.service.ISysRequestLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 请求日志 服务层处理。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Service
public class SysRequestLogServiceImpl implements ISysRequestLogService
{
    @Autowired
    private SysRequestLogMapper requestLogMapper;

    @Override
    public void insertRequestLog(SysRequestLog requestLog)
    {
        requestLogMapper.insertRequestLog(requestLog);
    }

    @Override
    public List<SysRequestLog> selectRequestLogList(SysRequestLog requestLog)
    {
        return requestLogMapper.selectRequestLogList(requestLog);
    }

    @Override
    public SysRequestLog selectRequestLogById(Long requestId)
    {
        return requestLogMapper.selectRequestLogById(requestId);
    }

    @Override
    public int deleteRequestLogByIds(Long[] requestIds)
    {
        return requestLogMapper.deleteRequestLogByIds(requestIds);
    }

    @Override
    public void cleanRequestLog()
    {
        requestLogMapper.cleanRequestLog();
    }
}
