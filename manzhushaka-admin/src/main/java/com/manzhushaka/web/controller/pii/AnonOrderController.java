package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.result.AnonInvoiceDownloadResult;
import com.manzhushaka.biz.pii.application.service.AnonOrderService;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Anonymous
@RestController
@RequestMapping("/anon/pii")
public class AnonOrderController extends BaseController {

    private final AnonOrderService anonOrderService;

    public AnonOrderController(AnonOrderService anonOrderService) {
        this.anonOrderService = anonOrderService;
    }

    @GetMapping("/order/{no}")
    public AjaxResult getOrder(@PathVariable String no,
                               @RequestParam String token) {
        return success(anonOrderService.getOrder(no, token));
    }

    @GetMapping("/invoice/{no}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String no,
                                                  @RequestParam String token) {
        AnonInvoiceDownloadResult result = anonOrderService.downloadInvoice(no, token);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(result.getFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(result.getContent());
    }
}
