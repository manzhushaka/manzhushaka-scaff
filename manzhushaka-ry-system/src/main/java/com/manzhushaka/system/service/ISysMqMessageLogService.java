package com.manzhushaka.system.service;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;

/**
 * 消息队列台账 服务层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface ISysMqMessageLogService
{
    /**
     * 获取或创建消息队列主台账记录。
     * <p>
     * 先根据 streamKey + messageId 唯一索引查询，存在则返回已有记录；
     * 不存在则插入新记录；并发冲突时重新查询。
     *
     * @param messageLog 消息队列主台账
     * @return 主台账记录
     */
    SysMqMessageLog createOrGetMessageLog(SysMqMessageLog messageLog);

    /**
     * 更新消息队列主台账。
     *
     * @param messageLog 消息队列主台账
     */
    void updateMessageLog(SysMqMessageLog messageLog);

    /**
     * 新增消息队列执行明细。
     *
     * @param detail 消息队列执行明细
     * @return 带主键的执行明细
     */
    SysMqMessageLogDetail insertMessageLogDetail(SysMqMessageLogDetail detail);

    /**
     * 更新消息队列执行明细。
     *
     * @param detail 消息队列执行明细
     */
    void updateMessageLogDetail(SysMqMessageLogDetail detail);

    /**
     * 查询消息队列主台账列表。
     *
     * @param messageLog 消息队列主台账查询条件
     * @return 消息队列主台账列表
     */
    List<SysMqMessageLog> selectMessageLogList(SysMqMessageLog messageLog);

    /**
     * 根据主键查询消息队列主台账。
     *
     * @param messageLogId 消息台账主键
     * @return 消息队列主台账
     */
    SysMqMessageLog selectMessageLogById(Long messageLogId);

    /**
     * 根据消息台账主键查询执行明细列表。
     *
     * @param messageLogId 消息台账主键
     * @return 执行明细列表
     */
    List<SysMqMessageLogDetail> selectDetailListByMessageLogId(Long messageLogId);

    /**
     * 批量删除消息队列主台账及其明细。
     *
     * @param messageLogIds 消息台账主键数组
     * @return 影响行数
     */
    int deleteMessageLogByIds(Long[] messageLogIds);

    /**
     * 清空消息队列主台账及明细。
     */
    void cleanMessageLog();
}