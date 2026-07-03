package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.service.AnonQrcodeService;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/anon/pii/qrcode")
public class AnonQrcodeQueryController extends BaseController {

    private final AnonQrcodeService anonQrcodeService;

    public AnonQrcodeQueryController(AnonQrcodeService anonQrcodeService) {
        this.anonQrcodeService = anonQrcodeService;
    }

    @GetMapping("/{code}")
    public AjaxResult getByCode(@PathVariable String code) {
        return success(anonQrcodeService.getConfig(code));
    }
}
