package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.PrecreatePayCommand;
import com.manzhushaka.biz.pii.application.service.AnonPayService;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.utils.ip.IpUtils;
import com.manzhushaka.web.dto.pii.PrecreatePayRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/anon/pii/pay")
public class AnonPayController extends BaseController {

    private final AnonPayService anonPayService;

    public AnonPayController(AnonPayService anonPayService) {
        this.anonPayService = anonPayService;
    }

    @PostMapping("/precreate")
    public AjaxResult precreate(@Validated @RequestBody PrecreatePayRequest request,
                                HttpServletRequest servletRequest) {
        return success(anonPayService.precreate(new PrecreatePayCommand(
                request.getCode(),
                request.getTaxItemId(),
                request.getAmount(),
                request.getBuyerName(),
                request.getBuyerTaxCode(),
                request.getBuyerEmail(),
                request.getBuyerMobile(),
                request.getOpenid(),
                IpUtils.getIpAddr(servletRequest)
        )));
    }
}
