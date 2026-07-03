package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.service.InvoiceNotifyService;
import com.manzhushaka.common.annotation.Anonymous;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/pii/invoice")
public class InvoiceNotifyController {

    private final InvoiceNotifyService invoiceNotifyService;

    public InvoiceNotifyController(InvoiceNotifyService invoiceNotifyService) {
        this.invoiceNotifyService = invoiceNotifyService;
    }

    @PostMapping("/notify")
    public String notify(@RequestBody String rawBody,
                         @RequestParam(required = false) String sign) {
        return invoiceNotifyService.notify(rawBody, sign);
    }
}
