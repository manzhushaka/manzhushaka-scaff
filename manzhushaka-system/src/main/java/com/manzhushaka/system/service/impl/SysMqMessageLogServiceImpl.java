package com.manzhushaka.system.service.impl;

import java.util.List;

import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.mapper.SysMqMessageLogDetailMapper;
import com.manzhushaka.system.mapper.SysMqMessageLogMapper;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 消息队列台账 服务层处理。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Service
public class SysMqMessageLogServiceImpl implements ISysMqMessageLogService
{
    @Autowired
    private SysMqMessageLogMapper mqMessageLogMapper;

    @Autowired
    private SysMqMessageLogDetailMapper mqMessageLogDetailMapper;

    @Override
    public SysMqMessageLog createOrGetMessageLog(SysMqMessageLog messageLog)
    {
        SysMqMessageLog existing = mqMessageLogMapper.selectMessageLogByStreamAndMessageId(
                messageLog.getStreamKey(), messageLog.getMessageId());
        if (existing != null)
        {
            return existing;
        }
        try
        {
            mqMessageLogMapper.insertMessageLog(messageLog);
            return messageLog;
        }
        catch (DuplicateKeyException ex)
        {
            return mqMessageLogMapper.selectMessageLogByStreamAndMessageId(
                    messageLog.getStreamKey(), messageLog.getMessageId());
        }
    }

    @Override
    public void updateMessageLog(SysMqMessageLog messageLog)
    {
        mqMessageLogMapper.updateMessageLog(messageLog);
    }

    @Override
    public SysMqMessageLogDetail insertMessageLogDetail(SysMqMessageLogDetail detail)
    {
        mqMessageLogDetailMapper.insertMessageLogDetail(detail);
        return detail;
    }

    @Override
    public void updateMessageLogDetail(SysMqMessageLogDetail detail)
    {
        mqMessageLogDetailMapper.updateMessageLogDetail(detail);
    }

    @Override
    public List<SysMqMessageLog> selectMessageLogList(SysMqMessageLog messageLog)
    {
        return mqMessageLogMapper.selectMessageLogList(messageLog);
    }

    @Override
    public SysMqMessageLog selectMessageLogById(Long messageLogId)
    {
        return mqMessageLogMapper.selectMessageLogById(messageLogId);
    }

    @Override
    public List<SysMqMessageLogDetail> selectDetailListByMessageLogId(Long messageLogId)
    {
        return mqMessageLogDetailMapper.selectDetailListByMessageLogId(messageLogId);
    }

    @Override
    public int deleteMessageLogByIds(Long[] messageLogIds)
    {
        mqMessageLogDetailMapper.deleteDetailByMessageLogIds(messageLogIds);
        return mqMessageLogMapper.deleteMessageLogByIds(messageLogIds);
    }

    @Override
    public void cleanMessageLog()
    {
        mqMessageLogDetailMapper.cleanMessageLogDetail();
        mqMessageLogMapper.cleanMessageLog();
    }
}