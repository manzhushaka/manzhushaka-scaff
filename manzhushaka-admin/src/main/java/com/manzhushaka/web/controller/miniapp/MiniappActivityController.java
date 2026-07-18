package com.manzhushaka.web.controller.miniapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
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
     * 查询当前生效活动。
     * 无当前活动时 data 为 null；有则返回活动全字段、merchantCount、couponCount 和 coupons。
     *
     * @return 当前生效活动
     */
    @Anonymous
    @Log(title = "小程序查询当前活动", businessType = BusinessType.OTHER,
            isSaveRequestData = false, isSaveResponseData = false)
    @GetMapping("/current")
    public AjaxResult current()
    {
        return success(activityAppService.getCurrentActivity());
    }

    /**
     * 查询全部生效活动列表。
     * 按优先级与开始时间倒序，包含地域维度、merchantCount 和 couponCount。
     *
     * @return 生效活动列表
     */
    @Anonymous
    @Log(title = "小程序查询活动列表", businessType = BusinessType.OTHER,
            isSaveRequestData = false, isSaveResponseData = false)
    @GetMapping("/list")
    public AjaxResult list()
    {
        return success(activityAppService.listActiveActivities());
    }
}
