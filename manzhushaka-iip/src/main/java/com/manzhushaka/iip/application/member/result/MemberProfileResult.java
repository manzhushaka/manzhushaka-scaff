package com.manzhushaka.iip.application.member.result;

/**
 * 小程序用户资料结果（登录响应与个人中心）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MemberProfileResult(Long memberId, String nickname, String avatar, String phone, Integer availablePoints,
        Integer totalPoints)
{
    @Override
    public String toString()
    {
        return "MemberProfileResult[memberId=" + memberId + ", nickname=" + nickname
                + ", availablePoints=" + availablePoints + ", totalPoints=" + totalPoints + "]";
    }
}
