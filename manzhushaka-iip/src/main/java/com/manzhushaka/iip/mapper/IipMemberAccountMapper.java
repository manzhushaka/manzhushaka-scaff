package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipMemberAccount;

/**
 * 用户平台账号 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipMemberAccountMapper
{
    /**
     * 通过ID查询平台账号
     * 
     * @param accountId 账号ID
     * @return 平台账号信息
     */
    public IipMemberAccount selectIipMemberAccountById(Long accountId);

    /**
     * 查询平台账号列表
     * 
     * @param iipMemberAccount 查询条件
     * @return 平台账号集合
     */
    public List<IipMemberAccount> selectIipMemberAccountList(IipMemberAccount iipMemberAccount);

    /**
     * 新增平台账号
     * 
     * @param iipMemberAccount 平台账号信息
     * @return 结果
     */
    public int insertIipMemberAccount(IipMemberAccount iipMemberAccount);

    /**
     * 修改平台账号
     * 
     * @param iipMemberAccount 平台账号信息
     * @return 结果
     */
    public int updateIipMemberAccount(IipMemberAccount iipMemberAccount);

    /**
     * 通过ID删除平台账号
     * 
     * @param accountId 账号ID
     * @return 结果
     */
    public int deleteIipMemberAccountById(Long accountId);

    /**
     * 批量删除平台账号
     * 
     * @param accountIds 需要删除的账号ID
     * @return 结果
     */
    public int deleteIipMemberAccountByIds(Long[] accountIds);

    /**
     * 按平台与openid查询平台账号（小程序登录定位用户）
     * 
     * @param platform 平台（wechat/alipay/unionpay）
     * @param openid 平台用户标识
     * @return 平台账号信息，不存在时返回null
     */
    public IipMemberAccount selectByPlatformOpenid(@Param("platform") String platform, @Param("openid") String openid);
}
