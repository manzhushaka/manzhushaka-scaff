package com.manzhushaka.iip.application.member.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.ip.IpUtils;
import com.manzhushaka.iip.application.member.command.MiniappLoginCommand;
import com.manzhushaka.iip.application.member.result.MemberProfileResult;
import com.manzhushaka.iip.application.member.service.MiniappAuthAppService;
import com.manzhushaka.iip.domain.IipMember;
import com.manzhushaka.iip.domain.IipPointsAccount;
import com.manzhushaka.iip.mapper.IipPointsAccountMapper;
import com.manzhushaka.iip.miniapp.MiniappLoginService;
import com.manzhushaka.iip.miniapp.MiniappLoginService.MiniappSession;
import com.manzhushaka.iip.service.IIipPointsService;
import com.manzhushaka.iip.service.impl.IipMemberServiceImpl;

/**
 * 小程序认证应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class MiniappAuthAppServiceImpl implements MiniappAuthAppService
{
    /** 状态：停用 */
    private static final String STATUS_DISABLED = "1";

    @Autowired
    private MiniappLoginService miniappLoginService;

    @Autowired
    private IipMemberServiceImpl memberService;

    @Autowired
    private IIipPointsService pointsService;

    /** 用户域只读引用积分域 Mapper 查询累计积分，符合项目跨域只读约定 */
    @Autowired
    private IipPointsAccountMapper pointsAccountMapper;

    @Override
    public MemberProfileResult login(MiniappLoginCommand command)
    {
        MiniappSession session = miniappLoginService.login(command.platform(), command.code());
        IipMember member = memberService.getOrCreateByOpenid(session.platform(), session.openid(), session.unionid(),
                command.nickname(), command.avatar());
        if (STATUS_DISABLED.equals(member.getStatus()))
        {
            throw new ServiceException("账号已被停用，请联系管理员");
        }
        memberService.updateLoginTrace(member.getMemberId(), IpUtils.getIpAddr());
        return toProfile(member);
    }

    @Override
    public MemberProfileResult getProfile(Long memberId)
    {
        IipMember member = memberService.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("用户不存在");
        }
        return toProfile(member);
    }

    private MemberProfileResult toProfile(IipMember member)
    {
        int availablePoints = pointsService.getAvailablePoints(member.getMemberId());
        IipPointsAccount account = pointsAccountMapper.selectByMemberId(member.getMemberId());
        int totalPoints = account == null || account.getTotalPoints() == null ? 0 : account.getTotalPoints();
        return new MemberProfileResult(member.getMemberId(), member.getNickname(), member.getAvatar(),
                member.getPhone(), availablePoints, totalPoints);
    }
}
