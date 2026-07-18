package com.manzhushaka.iip.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipPointsRecord;

/**
 * 积分流水 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipPointsRecordMapper
{
    /**
     * 通过ID查询积分流水
     * 
     * @param recordId 流水ID
     * @return 积分流水信息
     */
    public IipPointsRecord selectIipPointsRecordById(Long recordId);

    /**
     * 查询积分流水列表
     * 
     * @param iipPointsRecord 查询条件
     * @return 积分流水集合
     */
    public List<IipPointsRecord> selectIipPointsRecordList(IipPointsRecord iipPointsRecord);

    /**
     * 新增积分流水
     * 
     * @param iipPointsRecord 积分流水信息
     * @return 结果
     */
    public int insertIipPointsRecord(IipPointsRecord iipPointsRecord);

    /**
     * 修改积分流水
     * 
     * @param iipPointsRecord 积分流水信息
     * @return 结果
     */
    public int updateIipPointsRecord(IipPointsRecord iipPointsRecord);

    /**
     * 通过ID删除积分流水
     * 
     * @param recordId 流水ID
     * @return 结果
     */
    public int deleteIipPointsRecordById(Long recordId);

    /**
     * 批量删除积分流水
     * 
     * @param recordIds 需要删除的流水ID
     * @return 结果
     */
    public int deleteIipPointsRecordByIds(Long[] recordIds);

    /**
     * 查询用户可消耗的 earn 批次（remaining大于0且未过期），按创建时间升序供 FIFO 扣减
     * 
     * @param memberId 用户ID
     * @return earn 批次集合
     */
    public List<IipPointsRecord> selectEarnFifoList(Long memberId);

    /**
     * 查询已到期的 earn 批次（remaining大于0且过期时间不晚于指定时间）
     * 
     * @param now 当前时间
     * @return 过期 earn 批次集合
     */
    public List<IipPointsRecord> selectExpiredEarnList(@Param("now") Date now);

    /**
     * 将 earn 批次剩余未消耗清零（过期结转用）
     * 
     * @param recordId 流水ID
     * @return 结果
     */
    public int clearRemaining(Long recordId);

    /**
     * 原子扣减 earn 批次剩余未消耗数量（FIFO 消费用，剩余不足时不更新）
     * 
     * @param recordId 流水ID
     * @param points 扣减数量（正数）
     * @return 影响行数，0 表示批次剩余不足
     */
    public int decrRemaining(@Param("recordId") Long recordId, @Param("points") Integer points);

    /**
     * 按业务来源与业务单据ID查询流水（幂等检查）
     * 
     * @param bizType 业务来源
     * @param bizId 业务单据ID
     * @return 积分流水信息，不存在时返回null
     */
    public IipPointsRecord selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);
}
