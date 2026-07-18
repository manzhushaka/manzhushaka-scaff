package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.invoice.command.SubmitInvoiceCommand;
import com.manzhushaka.web.dto.miniapp.InvoiceSubmitRequest;

/**
 * 发票小程序端 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class InvoiceMiniappConverter
{
    private InvoiceMiniappConverter()
    {
    }

    /**
     * 提交请求转提交命令。
     *
     * @param request 小程序提交发票请求
     * @return 提交发票命令
     */
    public static SubmitInvoiceCommand toSubmitCommand(InvoiceSubmitRequest request)
    {
        return new SubmitInvoiceCommand(request.getMerchantId(), request.getMerchantName(),
                request.getInvoiceCode(), request.getInvoiceNo(), request.getInvoiceDate(),
                request.getAmount(), request.getImageUrl());
    }
}
