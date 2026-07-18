package com.manzhushaka.web.controller.miniapp;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.merchant.result.MerchantResult;
import com.manzhushaka.iip.application.merchant.result.VerifyRecordResult;
import com.manzhushaka.iip.application.merchant.service.MerchantAppService;
import com.manzhushaka.web.converter.iip.MerchantMiniappConverter;
import com.manzhushaka.web.dto.miniapp.MerchantApplyRequest;
import com.manzhushaka.web.dto.miniapp.MerchantVerifyRequest;

/**
 * 商户中心 信息操作处理（小程序端）。
 *
 * /miniapp/** 默认要求登录 token，接口内通过 SecurityContextHelper.getUserId() 取当前用户ID，
 * 商户能力以"当前用户绑定商户且状态正常"判定，不使用后台 perms 体系。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/miniapp/merchant")
public class MiniappMerchantController extends BaseController
{
    @Autowired
    private MerchantAppService merchantAppService;

    /**
     * 提交商户入驻申请（当前用户已绑定商户时报"已申请"，申请状态为待审核）
     */
    @Log(title = "小程序商户", businessType = BusinessType.INSERT)
    @PostMapping("/apply")
    public AjaxResult apply(@Validated @RequestBody MerchantApplyRequest request)
    {
        return success(merchantAppService.applyMerchant(MerchantMiniappConverter.toApplyCommand(request),
                SecurityContextHelper.getUserId()));
    }

    /**
     * 查询当前用户绑定的商户信息（未申请过时 data 为 null）
     */
    @Log(title = "小程序商户", businessType = BusinessType.OTHER)
    @GetMapping("/info")
    public AjaxResult info()
    {
        return success(merchantAppService.getMyMerchant(SecurityContextHelper.getUserId()));
    }

    /**
     * 核销用户券（仅状态正常的商户可操作，核销码校验未使用、未过期、限定商户一致）
     */
    @Log(title = "小程序商户", businessType = BusinessType.UPDATE)
    @PostMapping("/verify")
    public AjaxResult verify(@Validated @RequestBody MerchantVerifyRequest request)
    {
        return success(merchantAppService.verifyCoupon(MerchantMiniappConverter.toVerifyCommand(request),
                SecurityContextHelper.getUserId(), SecurityContextHelper.getUsername()));
    }

    /**
     * 查询本商户核销工作台统计（今日/累计的核销笔数与消耗积分，仅状态正常的商户可查询）
     */
    @Log(title = "小程序商户", businessType = BusinessType.OTHER)
    @GetMapping("/verify/stats")
    public AjaxResult verifyStats()
    {
        MerchantResult merchant = merchantAppService.getVerifiableMerchant(SecurityContextHelper.getUserId());
        return success(merchantAppService.getVerifyStats(merchant.merchantId()));
    }

    /**
     * 查询本商户核销记录。
     *
     * 分页参数 pageNum/pageSize 由请求查询参数传入（与后台列表一致，由 startPage 读取）；
     * days 可选（1/7/30 语义为最近 N 天，不传为全部）；
     * 返回 TableDataInfo（rows 为核销记录列表，total 为总数），仅状态正常的商户可查询。
     */
    @Log(title = "小程序商户", businessType = BusinessType.OTHER)
    @GetMapping("/verify/records")
    public TableDataInfo verifyRecords(@RequestParam(required = false) Integer days)
    {
        MerchantResult merchant = merchantAppService.getVerifiableMerchant(SecurityContextHelper.getUserId());
        startPage();
        List<VerifyRecordResult> list = merchantAppService.listVerifyRecords(merchant.merchantId(), days);
        return getDataTable(list);
    }
}
