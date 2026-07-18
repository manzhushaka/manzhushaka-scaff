package com.manzhushaka.iip.application.member.service;

import java.util.List;
import com.manzhushaka.iip.application.member.command.ChangeMemberStatusCommand;
import com.manzhushaka.iip.application.member.query.MemberQuery;
import com.manzhushaka.iip.application.member.result.MemberResult;

/**
 * 小程序用户后台管理应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipMemberAppService
{
    /**
     * 查询用户列表。
     *
     * @param query 查询条件
     * @return 用户列表
     */
    List<MemberResult> listMembers(MemberQuery query);

    /**
     * 查询用户详情。
     *
     * @param memberId 用户ID
     * @return 用户详情，不存在时返回 null
     */
    MemberResult getMember(Long memberId);

    /**
     * 修改用户状态（启停切换）。
     *
     * @param command 状态命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int changeMemberStatus(ChangeMemberStatusCommand command, String operatorUsername);
}
