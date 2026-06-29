package com.manzhushaka.system.mapper;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLog;

/**
 * 消息队列主台账 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface SysMqMessageLogMapper
{
    /**
     * 新增消息队列主台账。
     *
     * @param messageLog 消息队列主台账
     * @return 影响行数
     */
    int insertMessageLog(SysMqMessageLog messageLog);

    /**
     * 更新消息队列主台账。
     *
     * @param messageLog 消息队列主台账
     * @return 影响行数
     */
    int updateMessageLog(SysMqMessageLog messageLog);

    /**
     * 根据主键查询消息队列主台账。
     *
     * @param messageLogId 消息台账主键
     * @return 消息队列主台账
     */
    SysMqMessageLog selectMessageLogById(Long messageLogId);

    /**
     * 根据 Stream 和消息 ID 查询消息队列主台账。
     *
     * @param streamKey 原始 Stream
     * @param messageId Redis Stream 消息 ID
     * @return 消息队列主台账
     */
    SysMqMessageLog selectMessageLogByStreamAndMessageId(String streamKey, String messageId);

    /**
     * 查询消息队列主台账列表。
     *
     * @param messageLog 消息队列主台账查询条件
     * @return 消息队列主台账列表
     */
    List<SysMqMessageLog> selectMessageLogList(SysMqMessageLog messageLog);

    /**
     * 批量删除消息队列主台账。
     *
     * @param messageLogIds 消息台账主键数组
     * @return 影响行数
     */
    int deleteMessageLogByIds(Long[] messageLogIds);

    /**
     * 清空消息队列主台账。
     */
    void cleanMessageLog();
}