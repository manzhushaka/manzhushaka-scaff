package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.UpdateMerchantConfigCommand;
import com.manzhushaka.biz.pii.application.service.MerchantConfigService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.web.dto.pii.UpdateMerchantConfigRequest;
import com.manzhushaka.web.vo.pii.MerchantConfigVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pii/merchant/config")
public class MerchantConfigController extends BaseController {

    private final MerchantConfigService merchantConfigService;

    public MerchantConfigController(MerchantConfigService merchantConfigService) {
        this.merchantConfigService = merchantConfigService;
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:config')")
    @GetMapping("/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId) {
        return success(MerchantConfigVO.from(merchantConfigService.getByDeptId(deptId)));
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:config')")
    @Log(title = "商户参数配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateMerchantConfigRequest request) {
        return toAjax(merchantConfigService.update(toCommand(request)));
    }

    private UpdateMerchantConfigCommand toCommand(UpdateMerchantConfigRequest request) {
        return new UpdateMerchantConfigCommand(request.getDeptId(), request.getUmsMerchantId(),
                request.getUmsTerminalId(), request.getUmsPaySignKey(), request.getUmsInvoiceSignKey(),
                request.getInvoiceMsgSrc(), request.getInvoiceSellerName(), request.getInvoiceSellerTaxCode(),
                request.getInvoiceSellerAddress(), request.getInvoiceSellerTelephone(), request.getInvoiceSellerBank(),
                request.getInvoiceSellerAccount(), request.getInvoicePayee(), request.getInvoiceChecker(),
                request.getInvoiceDrawer(), request.getNotifyUrl(), request.getRemark());
    }
}
