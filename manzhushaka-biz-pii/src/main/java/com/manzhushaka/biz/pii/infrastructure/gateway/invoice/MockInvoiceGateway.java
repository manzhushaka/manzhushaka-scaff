package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.QueryRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.QueryResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockInvoiceGateway implements InvoiceGateway {

    @Override
    public InvoiceResponse invoice(InvoiceRequest request) {
        InvoiceResponse response = new InvoiceResponse();
        response.setStatus("ISSUED");
        response.setInvoiceNo("MOCK_INV_" + UUID.randomUUID().toString().substring(0, 8));
        response.setInvoiceCode("MOCK_CODE");
        response.setMerchantId(request.getMerchantId());
        response.setTerminalId(request.getTerminalId());
        response.setMerOrderId(request.getMerOrderId());
        response.setMerOrderDate(request.getMerOrderDate());
        response.setBuyerName(request.getBuyerName());
        response.setTotalPriceIncludingTax(toYuan(request.getAmount()));
        response.setTotalTax(0.0);
        response.setTotalPrice(toYuan(request.getAmount()));
        response.setPdfUrl("https://mock.local/invoice/" + response.getInvoiceNo() + ".pdf");
        response.setResultCode("SUCCESS");
        response.setResultMsg("MOCK OK");
        return response;
    }

    @Override
    public ReverseResponse reverse(ReverseRequest request) {
        ReverseResponse response = new ReverseResponse();
        response.setStatus("REVERSED");
        response.setMerchantId(request.getMerchantId());
        response.setTerminalId(request.getTerminalId());
        response.setMerOrderId(request.getMerOrderId());
        response.setMerOrderDate(request.getMerOrderDate());
        response.setResultCode("SUCCESS");
        response.setResultMsg("MOCK REVERSE OK");
        return response;
    }

    @Override
    public QueryResponse query(QueryRequest request) {
        QueryResponse response = new QueryResponse();
        response.setStatus("ISSUED");
        response.setMerchantId(request.getMerchantId());
        response.setTerminalId(request.getTerminalId());
        response.setMerOrderId(request.getMerOrderId());
        response.setMerOrderDate(request.getMerOrderDate());
        response.setInvoiceNo("MOCK_INV_QUERY");
        response.setInvoiceCode("MOCK_CODE");
        response.setPdfUrl("https://mock.local/invoice/MOCK_INV_QUERY.pdf");
        response.setResultCode("SUCCESS");
        response.setResultMsg("MOCK QUERY OK");
        return response;
    }

    @Override
    public PickupResponse pickup(PickupRequest request) {
        PickupResponse response = new PickupResponse();
        response.setResultCode("SUCCESS");
        response.setResultMsg("MOCK PICKUP OK");
        response.setPdf("JVBERi0xLjQKJU1PQ0sgUERG");
        response.setPdfUrl("https://mock.local/invoice/mock.pdf");
        response.setOfdUrl("https://mock.local/invoice/mock.ofd");
        response.setXmlUrl("https://mock.local/invoice/mock.xml");
        return response;
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        NotifyPayload payload = new NotifyPayload();
        payload.setStatus("ISSUED");
        payload.setMerOrderId("MOCK_ORDER");
        payload.setMerOrderDate("20260101");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("status", payload.getStatus());
        params.put("merOrderId", payload.getMerOrderId());
        params.put("merOrderDate", payload.getMerOrderDate());

        NotifyVerifyResult result = new NotifyVerifyResult();
        result.setValid(true);
        result.setPayload(payload);
        result.setParams(params);
        return result;
    }

    private Double toYuan(Long amount) {
        return amount == null ? null : amount / 100.0;
    }
}
