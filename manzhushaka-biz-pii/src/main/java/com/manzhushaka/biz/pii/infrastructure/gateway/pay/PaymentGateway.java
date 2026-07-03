package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundResponse;

public interface PaymentGateway {
    PreCreateResponse preCreate(PreCreateRequest request);
    RefundResponse refund(RefundRequest request);
    NotifyVerifyResult verifyAndParse(String rawBody, String signKey);
}
