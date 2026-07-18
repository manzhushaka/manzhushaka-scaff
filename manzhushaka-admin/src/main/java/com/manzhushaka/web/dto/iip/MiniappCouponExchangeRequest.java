package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotNull;

/**
 * 小程序兑换券请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MiniappCouponExchangeRequest
{
    @NotNull(message = "券ID不能为空")
    private Long couponId;

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }
}
