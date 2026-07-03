package com.manzhushaka.biz.pii.application.command;

public record PrecreatePayCommand(
        String code,
        Long taxItemId,
        Long amount,
        String buyerName,
        String buyerTaxCode,
        String buyerEmail,
        String buyerMobile,
        String openid,
        String clientIp
) {
}
