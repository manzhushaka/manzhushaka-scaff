package com.manzhushaka.iip.application.member.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.application.member.command.ChangeMemberStatusCommand;
import com.manzhushaka.iip.application.member.query.MemberQuery;
import com.manzhushaka.iip.application.member.result.MemberResult;
import com.manzhushaka.iip.application.member.service.IipMemberAppService;
import com.manzhushaka.iip.domain.IipMember;
import com.manzhushaka.iip.service.impl.IipMemberServiceImpl;

/**
 * 小程序用户后台管理应用服务实现。
 * 
 * 依赖本域服务实现类 IipMemberServiceImpl：契约接口 IIipMemberService 仅暴露跨域方法且签名冻结，
 * 后台管理所需的列表/状态能力由实现类承载（Spring Boot 默认 CGLIB 代理，事务语义不受影响）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipMemberAppServiceImpl implements IipMemberAppService
{
    /** 状态：正常 */
    private static final String STATUS_NORMAL = "0";

    /** 状态：停用 */
    private static final String STATUS_DISABLED = "1";

    @Autowired
    private IipMemberServiceImpl memberService;

    @Override
    public List<MemberResult> listMembers(MemberQuery query)
    {
        return memberService.selectMemberList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public MemberResult getMember(Long memberId)
    {
        return toResult(memberService.selectMemberById(memberId));
    }

    @Override
    @Transactional
    public int changeMemberStatus(ChangeMemberStatusCommand command, String operatorUsername)
    {
        String status = command.status();
        if (!STATUS_NORMAL.equals(status) && !STATUS_DISABLED.equals(status))
        {
            throw new ServiceException("非法的用户状态");
        }
        IipMember member = memberService.selectMemberById(command.memberId());
        if (member == null)
        {
            throw new ServiceException("用户不存在");
        }
        return memberService.updateMemberStatus(command.memberId(), status, operatorUsername);
    }

    private IipMember toEntity(MemberQuery query)
    {
        IipMember member = new IipMember();
        if (query == null)
        {
            return member;
        }
        member.setPhone(query.phone());
        member.setStatus(query.status());
        putParam(member, "keyword", query.keyword());
        putParam(member, "beginTime", query.beginTime());
        putParam(member, "endTime", query.endTime());
        return member;
    }

    private void putParam(IipMember member, String name, String value)
    {
        if (value != null)
        {
            member.getParams().put(name, value);
        }
    }

    private MemberResult toResult(IipMember member)
    {
        if (member == null)
        {
            return null;
        }
        return new MemberResult(member.getMemberId(), member.getNickname(), member.getPhone(), member.getGender(),
                member.getStatus(), member.getLastLoginTime(), member.getLastLoginIp(), member.getCreateTime(),
                member.getRemark());
    }
}
