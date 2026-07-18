package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipPointsAccount;

/**
 * 积分账户 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipPointsAccountMapper
{
    /**
     * 通过ID查询积分账户
     * 
     * @param accountId 账户ID
     * @return 积分账户信息
     */
    public IipPointsAccount selectIipPointsAccountById(Long accountId);

    /**
     * 查询积分账户列表
     * 
     * @param iipPointsAccount 查询条件
     * @return 积分账户集合
     */
    public List<IipPointsAccount> selectIipPointsAccountList(IipPointsAccount iipPointsAccount);

    /**
     * 新增积分账户
     * 
     * @param iipPointsAccount 积分账户信息
     * @return 结果
     */
    public int insertIipPointsAccount(IipPointsAccount iipPointsAccount);

    /**
     * 修改积分账户
     * 
     * @param iipPointsAccount 积分账户信息
     * @return 结果
     */
    public int updateIipPointsAccount(IipPointsAccount iipPointsAccount);

    /**
     * 通过ID删除积分账户
     * 
     * @param accountId 账户ID
     * @return 结果
     */
    public int deleteIipPointsAccountById(Long accountId);

    /**
     * 批量删除积分账户
     * 
     * @param accountIds 需要删除的账户ID
     * @return 结果
     */
    public int deleteIipPointsAccountByIds(Long[] accountIds);

    /**
     * 按用户ID查询积分账户
     * 
     * @param memberId 用户ID
     * @return 积分账户信息，不存在时返回null
     */
    public IipPointsAccount selectByMemberId(Long memberId);

    /**
     * 原子增减可用与累计积分（发放/调整场景，points 可正可负）
     * 
     * @param memberId 用户ID
     * @param points 变动数量（正数增加、负数减少）
     * @return 影响行数，0 表示账户不存在
     */
    public int incrAvailable(@Param("memberId") Long memberId, @Param("points") Integer points);

    /**
     * 原子扣减可用积分并累加已用积分（消费场景，余额不足时不更新）
     * 
     * @param memberId 用户ID
     * @param points 消费数量（正数）
     * @return 影响行数，0 表示余额不足或账户不存在
     */
    public int incrUsed(@Param("memberId") Long memberId, @Param("points") Integer points);

    /**
     * 原子扣减可用积分并累加已过期积分（过期结转场景，余额不足时不更新）
     * 
     * @param memberId 用户ID
     * @param points 过期数量（正数）
     * @return 影响行数，0 表示余额不足或账户不存在
     */
    public int expirePoints(@Param("memberId") Long memberId, @Param("points") Integer points);

    /**
     * 管理端分页查询积分账户列表（关联 iip_member 取昵称）
     * 
     * @param memberId 用户ID，null 不限制
     * @param nickname 昵称关键字，null 或空不限制
     * @return 积分账户集合（含用户昵称）
     */
    public List<IipPointsAccount> selectAccountPageList(@Param("memberId") Long memberId, @Param("nickname") String nickname);
}
