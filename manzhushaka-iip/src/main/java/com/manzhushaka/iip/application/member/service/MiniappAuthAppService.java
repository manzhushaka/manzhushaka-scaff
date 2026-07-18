package com.manzhushaka.iip.application.member.service;

import com.manzhushaka.iip.application.member.command.MiniappLoginCommand;
import com.manzhushaka.iip.application.member.result.MemberProfileResult;

/**
 * 小程序认证应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface MiniappAuthAppService
{
    /**
     * 小程序登录：按平台 code 换取 openid，查询或创建用户并更新登录轨迹。
     *
     * @param command 登录命令
     * @return 用户资料（含可用积分）
     */
    MemberProfileResult login(MiniappLoginCommand command);

    /**
     * 查询当前登录用户资料。
     *
     * @param memberId 用户ID
     * @return 用户资料（含可用积分）
     */
    MemberProfileResult getProfile(Long memberId);
}
