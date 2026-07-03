package com.manzhushaka.biz.pii.application.command;

public record CreateMerchantCommand(
        Long parentDeptId,
        String merchantName,
        String adminUserName,
        String adminPassword,
        String adminPhone,
        String adminEmail,
        String umsMerchantId,
        String umsTerminalId,
        String umsPaySignKey,
        String umsInvoiceSignKey,
        String invoiceMsgSrc,
        Integer status,
        String remark
) {
}
