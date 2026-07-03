package com.manzhushaka.biz.pii.application.command;

public record UpdateMerchantCommand(
        Long id,
        String merchantName,
        String umsMerchantId,
        String umsTerminalId,
        String umsPaySignKey,
        String umsInvoiceSignKey,
        String invoiceMsgSrc,
        Integer status,
        String remark
) {
}
