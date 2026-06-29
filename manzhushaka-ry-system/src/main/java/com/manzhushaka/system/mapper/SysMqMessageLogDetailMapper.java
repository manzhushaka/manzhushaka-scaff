package com.manzhushaka.system.mapper;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;

/**
 * 消息队列执行明细 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface SysMqMessageLogDetailMapper
{
    /**
     * 新增消息队列执行明细。
     *
     * @param detail 消息队列执行明细
     * @return 影响行数
     */
    int insertMessageLogDetail(SysMqMessageLogDetail detail);

    /**
     * 更新消息队列执行明细。
     *
     * @param detail 消息队列执行明细
     * @return 影响行数
     */
    int updateMessageLogDetail(SysMqMessageLogDetail detail);

    /**
     * 根据消息台账主键查询执行明细列表。
     *
     * @param messageLogId 消息台账主键
     * @return 执行明细列表
     */
    List<SysMqMessageLogDetail> selectDetailListByMessageLogId(Long messageLogId);

    /**
     * 根据消息台账主键批量删除执行明细。
     *
     * @param messageLogIds 消息台账主键数组
     * @return 影响行数
     */
    int deleteDetailByMessageLogIds(Long[] messageLogIds);

    /**
     * 清空消息队列执行明细。
     */
    void cleanMessageLogDetail();
}