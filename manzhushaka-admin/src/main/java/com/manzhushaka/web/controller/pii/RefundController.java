package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.CreateRefundCommand;
import com.manzhushaka.biz.pii.application.service.RefundService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.web.dto.pii.CreateRefundRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pii/refund")
public class RefundController extends BaseController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PreAuthorize("@ss.hasPermi('biz:refund:add')")
    @Log(title = "退款管理", businessType = BusinessType.OTHER, isSaveRequestData = false)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateRefundRequest request) {
        return success(refundService.create(new CreateRefundCommand(
                request.getMerchantId(),
                request.getPayOrderId(),
                request.getAmount(),
                request.getReason(),
                getUserId()
        )));
    }
}
