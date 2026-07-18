package com.manzhushaka.web.controller.iip;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.result.activity.ActivityCouponResult;
import com.manzhushaka.iip.application.result.activity.ActivityMerchantResult;
import com.manzhushaka.iip.application.result.activity.ActivityResult;
import com.manzhushaka.iip.application.service.ActivityAppService;
import com.manzhushaka.web.converter.iip.ActivityAdminConverter;
import com.manzhushaka.web.dto.iip.ActivityCouponConfigRequest;
import com.manzhushaka.web.dto.iip.ActivityMerchantConfigRequest;
import com.manzhushaka.web.dto.iip.ActivityRequest;

/**
 * 活动管理 信息操作处理
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/activity")
public class IipActivityController extends BaseController
{
    @Autowired
    private ActivityAppService activityAppService;

    /**
     * 获取活动列表
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(ActivityRequest request)
    {
        startPage();
        List<ActivityResult> list = activityAppService.listActivities(ActivityAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 根据活动ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable Long activityId)
    {
        return success(activityAppService.getActivity(activityId));
    }

    /**
     * 新增活动
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:add')")
    @Log(title = "活动管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ActivityRequest request)
    {
        return toAjax(activityAppService.createActivity(ActivityAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改活动
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:edit')")
    @Log(title = "活动管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ActivityRequest request)
    {
        return toAjax(activityAppService.updateActivity(ActivityAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除活动（级联删除活动商户与活动券配置，进行中的活动禁止删除）
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:remove')")
    @Log(title = "活动管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        activityAppService.deleteActivities(activityIds);
        return success();
    }

    /**
     * 获取活动已配置商户列表（join iip_merchant 名称/类别/状态）
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @GetMapping("/merchants/{activityId}")
    public AjaxResult listMerchants(@PathVariable Long activityId)
    {
        List<ActivityMerchantResult> list = activityAppService.listActivityMerchants(activityId);
        return success(list);
    }

    /**
     * 新增活动商户配置
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @Log(title = "活动配置", businessType = BusinessType.INSERT)
    @PostMapping("/merchants")
    public AjaxResult addMerchant(@Validated @RequestBody ActivityMerchantConfigRequest request)
    {
        return toAjax(activityAppService.addActivityMerchant(ActivityAdminConverter.toMerchantCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除活动商户配置
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @Log(title = "活动配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/merchants/{id}")
    public AjaxResult removeMerchant(@PathVariable Long id)
    {
        return toAjax(activityAppService.removeActivityMerchant(id));
    }

    /**
     * 获取活动已配置券列表（join iip_coupon 名称/库存/已发数量）
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @GetMapping("/coupons/{activityId}")
    public AjaxResult listCoupons(@PathVariable Long activityId)
    {
        List<ActivityCouponResult> list = activityAppService.listActivityCoupons(activityId);
        return success(list);
    }

    /**
     * 新增活动券配置
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @Log(title = "活动配置", businessType = BusinessType.INSERT)
    @PostMapping("/coupons")
    public AjaxResult addCoupon(@Validated @RequestBody ActivityCouponConfigRequest request)
    {
        return toAjax(activityAppService.addActivityCoupon(ActivityAdminConverter.toCouponCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改活动券配置发行上限
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @Log(title = "活动配置", businessType = BusinessType.UPDATE)
    @PutMapping("/coupons")
    public AjaxResult editCoupon(@Validated @RequestBody ActivityCouponConfigRequest request)
    {
        return toAjax(activityAppService.updateActivityCoupon(ActivityAdminConverter.toCouponCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除活动券配置
     */
    @PreAuthorize("@ss.hasPermi('iip:activity:config')")
    @Log(title = "活动配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/coupons/{id}")
    public AjaxResult removeCoupon(@PathVariable Long id)
    {
        return toAjax(activityAppService.removeActivityCoupon(id));
    }
}
