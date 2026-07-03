package com.manzhushaka.biz.pii.application.query;

import java.time.LocalDateTime;

public record OrderPageQuery(Long merchantId, String outTradeNo, String payStatus, String invoiceStatus,
                             LocalDateTime payTimeBegin, LocalDateTime payTimeEnd) {
}
