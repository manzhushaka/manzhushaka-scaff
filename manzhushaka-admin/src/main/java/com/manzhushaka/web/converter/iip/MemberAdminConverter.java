package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.member.command.ChangeMemberStatusCommand;
import com.manzhushaka.iip.application.member.query.MemberQuery;
import com.manzhushaka.web.dto.iip.MemberRequest;
import com.manzhushaka.web.dto.iip.MemberStatusRequest;

/**
 * 小程序用户 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class MemberAdminConverter
{
    private MemberAdminConverter()
    {
    }

    public static MemberQuery toQuery(MemberRequest request)
    {
        return new MemberQuery(request.getKeyword(), request.getPhone(), request.getStatus(),
                request.getBeginTime(), request.getEndTime());
    }

    public static ChangeMemberStatusCommand toCommand(MemberStatusRequest request)
    {
        return new ChangeMemberStatusCommand(request.getMemberId(), request.getStatus());
    }
}
