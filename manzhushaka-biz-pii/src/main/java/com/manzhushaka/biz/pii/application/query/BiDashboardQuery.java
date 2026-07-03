package com.manzhushaka.biz.pii.application.query;

import java.time.LocalDateTime;

public record BiDashboardQuery(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
}
