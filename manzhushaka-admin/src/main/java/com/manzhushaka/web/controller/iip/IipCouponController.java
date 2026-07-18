package com.manzhushaka.web.controller.iip;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.coupon.result.CouponResult;
import com.manzhushaka.iip.application.coupon.service.CouponAppService;
import com.manzhushaka.web.converter.iip.CouponAdminConverter;
import com.manzhushaka.web.dto.iip.CouponRequest;

/**
 * 券管理 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/coupon")
public class IipCouponController extends BaseController
{
    @Autowired
    private CouponAppService couponAppService;

    /**
     * 获取券列表（按名称/类型/状态筛选）
     */
    @PreAuthorize("@ss.hasPermi('iip:coupon:list')")
    @GetMapping("/list")
    public TableDataInfo list(CouponRequest request)
    {
        startPage();
        List<CouponResult> list = couponAppService.listCoupons(CouponAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 导出券列表
     */
    @Log(title = "券管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('iip:coupon:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, CouponRequest request)
    {
        List<CouponResult> list = couponAppService.listCoupons(CouponAdminConverter.toQuery(request));
        ExcelUtil<CouponResult> util = new ExcelUtil<CouponResult>(CouponResult.class);
        util.exportExcel(response, list, "券数据");
    }

    /**
     * 根据券ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:coupon:query')")
    @GetMapping(value = "/{couponId}")
    public AjaxResult getInfo(@PathVariable Long couponId)
    {
        return success(couponAppService.getCoupon(couponId));
    }

    /**
     * 新增券
     */
    @PreAuthorize("@ss.hasPermi('iip:coupon:add')")
    @Log(title = "券管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CouponRequest request)
    {
        return toAjax(couponAppService.createCoupon(CouponAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改券（已产生兑换记录的券禁止修改积分价与有效期相关字段）
     */
    @PreAuthorize("@ss.hasPermi('iip:coupon:edit')")
    @Log(title = "券管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CouponRequest request)
    {
        return toAjax(couponAppService.updateCoupon(CouponAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除券（已产生兑换记录的券禁止删除）
     */
    @PreAuthorize("@ss.hasPermi('iip:coupon:remove')")
    @Log(title = "券管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{couponIds}")
    public AjaxResult remove(@PathVariable Long[] couponIds)
    {
        couponAppService.deleteCoupons(couponIds);
        return success();
    }
}
