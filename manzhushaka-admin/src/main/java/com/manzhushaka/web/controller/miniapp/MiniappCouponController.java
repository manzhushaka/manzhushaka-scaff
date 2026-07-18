package com.manzhushaka.web.controller.miniapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.coupon.service.MiniappCouponAppService;
import com.manzhushaka.web.dto.iip.MiniappCouponExchangeRequest;

/**
 * 小程序券 信息操作处理（积分商城、券详情、兑换、我的券）
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/miniapp/coupon")
public class MiniappCouponController extends BaseController
{
    @Autowired
    private MiniappCouponAppService couponAppService;

    /**
     * 获取积分商城券列表（上架且在兑换窗口内，category 可选筛选券品类）
     */
    @GetMapping("/mall")
    public AjaxResult mall(@RequestParam(value = "category", required = false) String category)
    {
        return success(couponAppService.listMallCoupons(category));
    }

    /**
     * 获取我的券列表（status 可选 0未使用/1已使用/2已过期）
     */
    @GetMapping("/mine")
    public AjaxResult mine(@RequestParam(value = "status", required = false) String status)
    {
        return success(couponAppService.listMyCoupons(SecurityContextHelper.getUserId(), status));
    }

    /**
     * 获取券详情（含当前用户已兑数量）
     */
    @GetMapping(value = "/{couponId}")
    public AjaxResult detail(@PathVariable Long couponId)
    {
        return success(couponAppService.getCouponDetail(couponId, SecurityContextHelper.getUserId()));
    }

    /**
     * 兑换券（返回含核销码的券实例）
     */
    @PostMapping("/exchange")
    public AjaxResult exchange(@Validated @RequestBody MiniappCouponExchangeRequest request)
    {
        return success(couponAppService.exchange(SecurityContextHelper.getUserId(), request.getCouponId()));
    }
}
