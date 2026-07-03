package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentGatewayTest {

    @Test
    void mockGatewayShouldReturnFrontendPayParameters() {
        MockPaymentGateway gateway = new MockPaymentGateway();

        PreCreateResponse response = gateway.preCreate(preCreateRequest());

        assertThat(response.getPrepayId()).startsWith("MOCK_PREPAY_");
        assertThat(response.getPackageStr()).isEqualTo("prepay_id=" + response.getPrepayId());
        assertThat(response.getNonceStr()).isNotBlank();
        assertThat(response.getTimestamp()).isNotBlank();
        assertThat(response.getSignType()).isEqualTo("MOCK");
        assertThat(response.getJsApiPaySign()).isEqualTo("MOCK_SIGN");
    }

    @Test
    void mockGatewayShouldReturnSuccessfulRefund() {
        MockPaymentGateway gateway = new MockPaymentGateway();

        RefundResponse response = gateway.refund(refundRequest());

        assertThat(response.getErrCode()).isEqualTo("000000");
        assertThat(response.getErrMsg()).isEqualTo("MOCK OK");
        assertThat(response.getTradeNo()).startsWith("MOCK_TRADE_");
        assertThat(response.getRefundOrderId()).isEqualTo("REFUND001");
    }

    @Test
    void umsGatewayShouldVerifyAndParseSignedNotifyJson() {
        UmsMpPaymentGateway gateway = new UmsMpPaymentGateway();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("outTradeNo", "PAY001");
        params.put("tradeNo", "UMS001");
        params.put("tradeStatus", "SUCCESS");
        params.put("sign", NotifyVerifier.sign(params, "K"));

        NotifyVerifyResult result = gateway.verifyAndParse(toJson(params), "K");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPayload().getOutTradeNo()).isEqualTo("PAY001");
        assertThat(result.getPayload().getTradeNo()).isEqualTo("UMS001");
        assertThat(result.getPayload().getTradeStatus()).isEqualTo("SUCCESS");
        assertThat(result.getParams()).containsEntry("sign", params.get("sign"));
    }

    @Test
    void umsGatewayShouldReturnInvalidWhenNotifySignIsWrong() {
        UmsMpPaymentGateway gateway = new UmsMpPaymentGateway();

        NotifyVerifyResult result = gateway.verifyAndParse("{\"outTradeNo\":\"PAY001\",\"sign\":\"BAD\"}", "K");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMsg()).isEqualTo("invalid sign");
    }

    private PreCreateRequest preCreateRequest() {
        PreCreateRequest request = new PreCreateRequest();
        request.setAppId("APP");
        request.setAppKey("APPKEY");
        request.setMerchantId("MID");
        request.setTerminalId("TID");
        request.setOutTradeNo("PAY001");
        request.setMerOrderDate("20260703");
        request.setTotalAmount(100L);
        request.setOpenid("OPENID");
        request.setNotifyUrl("https://example.com/notify");
        request.setOrderDesc("测试订单");
        request.setInstMid("YUEDANDEFAULT");
        request.setProd(false);
        return request;
    }

    private RefundRequest refundRequest() {
        RefundRequest request = new RefundRequest();
        request.setAppId("APP");
        request.setAppKey("APPKEY");
        request.setMerchantId("MID");
        request.setTerminalId("TID");
        request.setOutTradeNo("PAY001");
        request.setRefundOrderId("REFUND001");
        request.setRefundAmount(100L);
        request.setInstMid("YUEDANDEFAULT");
        request.setProd(false);
        return request;
    }

    private String toJson(Map<String, String> params) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        return json.append("}").toString();
    }
}
