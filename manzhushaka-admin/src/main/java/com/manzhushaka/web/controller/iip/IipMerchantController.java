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
import com.manzhushaka.iip.application.merchant.result.MerchantResult;
import com.manzhushaka.iip.application.merchant.service.MerchantAppService;
import com.manzhushaka.web.converter.iip.MerchantAdminConverter;
import com.manzhushaka.web.dto.iip.MerchantAuditRequest;
import com.manzhushaka.web.dto.iip.MerchantRequest;

/**
 * 商户管理 信息操作处理（管理端）
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/merchant")
public class IipMerchantController extends BaseController
{
    @Autowired
    private MerchantAppService merchantAppService;

    /**
     * 获取商户列表
     */
    @Log(title = "商户管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('iip:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(MerchantRequest request)
    {
        startPage();
        List<MerchantResult> list = merchantAppService.listMerchants(MerchantAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 导出商户列表
     */
    @Log(title = "商户管理", businessType = BusinessType.EXPORT, isSaveResponseData = false)
    @PreAuthorize("@ss.hasPermi('iip:merchant:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, MerchantRequest request)
    {
        List<MerchantResult> list = merchantAppService.listMerchants(MerchantAdminConverter.toQuery(request));
        ExcelUtil<MerchantResult> util = new ExcelUtil<MerchantResult>(MerchantResult.class);
        util.exportExcel(response, list, "商户数据");
    }

    /**
     * 根据商户ID获取详细信息
     */
    @Log(title = "商户管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('iip:merchant:query')")
    @GetMapping(value = "/getInfo/{merchantId}")
    public AjaxResult getInfo(@PathVariable Long merchantId)
    {
        return success(merchantAppService.getMerchant(merchantId));
    }

    /**
     * 新增商户
     */
    @Log(title = "商户管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('iip:merchant:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MerchantRequest request)
    {
        return toAjax(merchantAppService.createMerchant(MerchantAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改商户
     */
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('iip:merchant:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MerchantRequest request)
    {
        return toAjax(merchantAppService.updateMerchant(MerchantAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除商户
     */
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('iip:merchant:remove')")
    @DeleteMapping("/{merchantIds}")
    public AjaxResult remove(@PathVariable Long[] merchantIds)
    {
        merchantAppService.deleteMerchants(merchantIds);
        return success();
    }

    /**
     * 审核商户（仅待审核商户可审，通过置正常、驳回置停用并记录审核备注）
     */
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('iip:merchant:audit')")
    @PutMapping("/audit")
    public AjaxResult audit(@Validated @RequestBody MerchantAuditRequest request)
    {
        merchantAppService.auditMerchant(MerchantAdminConverter.toAuditCommand(request),
                SecurityContextHelper.getUsername());
        return success();
    }
}
