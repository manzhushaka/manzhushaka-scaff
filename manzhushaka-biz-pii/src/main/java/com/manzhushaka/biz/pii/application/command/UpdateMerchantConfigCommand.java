package com.manzhushaka.biz.pii.application.command;

public record UpdateMerchantConfigCommand(
        Long deptId,
        String umsMerchantId,
        String umsTerminalId,
        String umsPaySignKey,
        String umsInvoiceSignKey,
        String invoiceMsgSrc,
        String invoiceSellerName,
        String invoiceSellerTaxCode,
        String invoiceSellerAddress,
        String invoiceSellerTelephone,
        String invoiceSellerBank,
        String invoiceSellerAccount,
        String invoicePayee,
        String invoiceChecker,
        String invoiceDrawer,
        String notifyUrl,
        String remark
) {
}
