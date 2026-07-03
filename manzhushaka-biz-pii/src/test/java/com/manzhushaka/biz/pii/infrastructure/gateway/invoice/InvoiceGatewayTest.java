package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceGatewayTest {

    @Test
    void mockGatewayShouldIssueReverseAndPickupInvoice() {
        MockInvoiceGateway gateway = new MockInvoiceGateway();
        InvoiceRequest request = invoiceRequest();

        InvoiceResponse invoice = gateway.invoice(request);
        ReverseResponse reverse = gateway.reverse(reverseRequest());
        PickupResponse pickup = gateway.pickup(pickupRequest());

        assertThat(invoice.getResultCode()).isEqualTo("SUCCESS");
        assertThat(invoice.getStatus()).isEqualTo("ISSUED");
        assertThat(invoice.getInvoiceNo()).startsWith("MOCK_INV_");
        assertThat(invoice.getPdfUrl()).contains(invoice.getInvoiceNo());
        assertThat(reverse.getStatus()).isEqualTo("REVERSED");
        assertThat(reverse.getResultCode()).isEqualTo("SUCCESS");
        assertThat(pickup.getPdf()).isNotBlank();
        assertThat(pickup.getPdfUrl()).endsWith(".pdf");
    }

    @Test
    void umsGatewayShouldBuildSignedIssueBodyWithoutLeakingSignKey() {
        UmsInvoiceGateway gateway = new UmsInvoiceGateway();

        Map<String, Object> body = gateway.buildSignedBody("complex.issue", invoiceRequest());

        assertThat(body).containsEntry("msgType", "complex.issue");
        assertThat(body).containsEntry("merchantId", "MID");
        assertThat(body).containsEntry("goodsDetail", "[{\"name\":\"测试\",\"amount\":100}]");
        assertThat(body).doesNotContainKey("signKey");
        assertThat(body.get("sign")).isEqualTo(NotifyVerifier.sign(body, "K"));
    }

    @Test
    void umsGatewayShouldVerifyAndParseSignedNotifyJson() {
        UmsInvoiceGateway gateway = new UmsInvoiceGateway();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("merOrderId", "ORDER001");
        params.put("merOrderDate", "20260703");
        params.put("status", "ISSUED");
        params.put("invoiceNo", "INV001");
        params.put("sign", NotifyVerifier.sign(params, "K"));

        NotifyVerifyResult result = gateway.verifyAndParse(toJson(params), "K");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPayload().getMerOrderId()).isEqualTo("ORDER001");
        assertThat(result.getPayload().getStatus()).isEqualTo("ISSUED");
        assertThat(result.getPayload().getInvoiceNo()).isEqualTo("INV001");
    }

    @Test
    void umsGatewayShouldReturnInvalidWhenNotifySignIsWrong() {
        UmsInvoiceGateway gateway = new UmsInvoiceGateway();

        NotifyVerifyResult result = gateway.verifyAndParse("{\"merOrderId\":\"ORDER001\",\"sign\":\"BAD\"}", "K");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMsg()).isEqualTo("invalid sign");
    }

    private InvoiceRequest invoiceRequest() {
        InvoiceRequest request = new InvoiceRequest();
        request.setMerchantId("MID");
        request.setTerminalId("TID");
        request.setMerOrderDate("20260703");
        request.setMerOrderId("ORDER001");
        request.setBuyerName("张三");
        request.setAmount(100L);
        request.setGoodsDetail("[{\"name\":\"测试\",\"amount\":100}]");
        request.setMsgSrc("PII");
        request.setMsgId("MSG001");
        request.setNotifyUrl("https://example.com/invoice/notify");
        request.setSignKey("K");
        return request;
    }

    private ReverseRequest reverseRequest() {
        ReverseRequest request = new ReverseRequest();
        request.setMerchantId("MID");
        request.setTerminalId("TID");
        request.setMerOrderDate("20260703");
        request.setMerOrderId("ORDER001");
        request.setSignKey("K");
        return request;
    }

    private PickupRequest pickupRequest() {
        PickupRequest request = new PickupRequest();
        request.setMerchantId("MID");
        request.setTerminalId("TID");
        request.setMerOrderDate("20260703");
        request.setMerOrderId("ORDER001");
        request.setSignKey("K");
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
