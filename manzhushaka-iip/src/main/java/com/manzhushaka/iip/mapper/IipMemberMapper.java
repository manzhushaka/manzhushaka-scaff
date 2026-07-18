package com.manzhushaka.iip.mapper;

import java.util.List;
import com.manzhushaka.iip.domain.IipMember;

/**
 * 小程序用户 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipMemberMapper
{
    /**
     * 通过ID查询用户
     * 
     * @param memberId 用户ID
     * @return 用户信息
     */
    public IipMember selectIipMemberById(Long memberId);

    /**
     * 查询用户列表
     * 
     * @param iipMember 查询条件
     * @return 用户集合
     */
    public List<IipMember> selectIipMemberList(IipMember iipMember);

    /**
     * 新增用户
     * 
     * @param iipMember 用户信息
     * @return 结果
     */
    public int insertIipMember(IipMember iipMember);

    /**
     * 修改用户
     * 
     * @param iipMember 用户信息
     * @return 结果
     */
    public int updateIipMember(IipMember iipMember);

    /**
     * 通过ID删除用户
     * 
     * @param memberId 用户ID
     * @return 结果
     */
    public int deleteIipMemberById(Long memberId);

    /**
     * 批量删除用户
     * 
     * @param memberIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteIipMemberByIds(Long[] memberIds);
}
