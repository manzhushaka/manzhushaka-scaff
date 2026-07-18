package com.manzhushaka.web.controller.miniapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.iip.application.service.ActivityAppService;

/**
 * 小程序活动 信息操作处理
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/miniapp/activity")
public class MiniappActivityController extends BaseController
{
    @Autowired
    private ActivityAppService activityAppService;

    /**
     * 查询当前生效活动（无当前活动时data为null；有则返回活动全字段+merchantCount+couponCount+coupons）
     */
    @GetMapping("/current")
    public AjaxResult current()
    {
        return success(activityAppService.getCurrentActivity());
    }

    /**
     * 查询全部生效活动列表（按优先级与开始时间倒序，含地域维度与merchantCount/couponCount）
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        return success(activityAppService.listActiveActivities());
    }
}
