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
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.exception.ServiceException;
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
     * 获取积分商城券列表（上架且在兑换窗口内，category 可选筛选券品类，游客可访问）
     */
    @Anonymous
    @GetMapping("/mall")
    public AjaxResult mall(@RequestParam(value = "category", required = false) String category)
    {
        return success(couponAppService.listMallCoupons(category));
    }

    /**
     * 获取我的券列表（status 可选 0未使用/1已使用/2已过期/3已作废）
     */
    @GetMapping("/mine")
    public AjaxResult mine(@RequestParam(value = "status", required = false) String status)
    {
        return success(couponAppService.listMyCoupons(SecurityContextHelper.getUserId(), status));
    }

    /**
     * 获取券详情（含当前用户已兑数量，游客可访问，游客时已兑数量按 0 返回）
     */
    @Anonymous
    @GetMapping(value = "/{couponId}")
    public AjaxResult detail(@PathVariable Long couponId)
    {
        return success(couponAppService.getCouponDetail(couponId, getLoginUserIdOrNull()));
    }

    /**
     * 获取当前登录用户ID，未登录（游客）时返回 null。
     *
     * 商城与券详情已对游客开放，SecurityContextHelper.getUserId() 在匿名访问时抛出
     * 401 ServiceException，此处捕获后按游客处理，不影响详情主流程。
     *
     * @return 当前用户ID，游客返回 null
     */
    private Long getLoginUserIdOrNull()
    {
        try
        {
            return SecurityContextHelper.getUserId();
        }
        catch (ServiceException e)
        {
            // 游客访问时按未登录处理，已兑数量按 0 返回，其余数据正常展示
            return null;
        }
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
