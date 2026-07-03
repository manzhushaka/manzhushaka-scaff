package com.manzhushaka.biz.pii.application.command;

public record CreateRefundCommand(
        Long merchantId,
        Long payOrderId,
        Long amount,
        String reason,
        Long operatorId
) {
}
