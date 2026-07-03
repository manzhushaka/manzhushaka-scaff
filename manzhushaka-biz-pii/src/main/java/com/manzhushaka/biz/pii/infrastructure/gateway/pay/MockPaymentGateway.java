package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PreCreateResponse preCreate(PreCreateRequest request) {
        String prepayId = "MOCK_PREPAY_" + randomId();
        PreCreateResponse response = new PreCreateResponse();
        response.setPrepayId(prepayId);
        response.setNonceStr(randomId());
        response.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
        response.setSignType("MOCK");
        response.setJsApiPaySign("MOCK_SIGN");
        response.setPackageStr("prepay_id=" + prepayId);
        response.setPayUrl("mock://pay/" + prepayId);
        return response;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        RefundResponse response = new RefundResponse();
        response.setErrCode("000000");
        response.setErrMsg("MOCK OK");
        response.setTradeNo("MOCK_TRADE_" + randomId());
        response.setRefundOrderId(request.getRefundOrderId());
        response.setRefundStatus("SUCCESS");
        return response;
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        NotifyPayload payload = new NotifyPayload();
        payload.setOutTradeNo("MOCK_OUT_TRADE");
        payload.setTradeNo("MOCK_TRADE");
        payload.setTradeStatus("SUCCESS");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("outTradeNo", payload.getOutTradeNo());
        params.put("tradeNo", payload.getTradeNo());
        params.put("tradeStatus", payload.getTradeStatus());

        NotifyVerifyResult result = new NotifyVerifyResult();
        result.setValid(true);
        result.setPayload(payload);
        result.setParams(params);
        return result;
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
