package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.service.PayNotifyService;
import com.manzhushaka.common.annotation.Anonymous;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/pii/pay")
public class PayNotifyController {

    private final PayNotifyService payNotifyService;

    public PayNotifyController(PayNotifyService payNotifyService) {
        this.payNotifyService = payNotifyService;
    }

    @PostMapping("/notify")
    public String notify(@RequestBody String rawBody,
                         @RequestParam(required = false) String sign) {
        return payNotifyService.notify(rawBody, sign);
    }
}
