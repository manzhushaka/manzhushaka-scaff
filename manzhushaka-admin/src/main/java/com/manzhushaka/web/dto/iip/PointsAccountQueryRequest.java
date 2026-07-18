package com.manzhushaka.web.dto.iip;

/**
 * 积分账户查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class PointsAccountQueryRequest
{
    private Long memberId;
    private String nickname;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
}
