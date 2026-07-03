package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
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
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import com.manzhushaka.common.exception.ServiceException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "REAL")
public class UmsInvoiceGateway implements InvoiceGateway {

    @Autowired(required = false)
    private PiiProperties properties;

    @Override
    public InvoiceResponse invoice(InvoiceRequest request) {
        if (!hasText(request.getMerOrderDate())) {
            request.setMerOrderDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        }
        return doRequest("complex.issue", InvoiceResponse.class, request);
    }

    @Override
    public ReverseResponse reverse(ReverseRequest request) {
        return doRequest("complex.reverse", ReverseResponse.class, request);
    }

    @Override
    public QueryResponse query(QueryRequest request) {
        return doRequest("complex.query", QueryResponse.class, request);
    }

    @Override
    public PickupResponse pickup(PickupRequest request) {
        return doRequest("complex.pickup", PickupResponse.class, request);
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        NotifyVerifyResult result = new NotifyVerifyResult();
        try {
            Map<String, String> params = parseJsonMap(rawBody);
            result.setParams(params);
            if (!NotifyVerifier.verify(params, signKey)) {
                result.setValid(false);
                result.setErrorMsg("invalid sign");
                return result;
            }
            result.setValid(true);
            result.setPayload(toPayload(params));
            return result;
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMsg(e.getMessage());
            return result;
        }
    }

    Map<String, Object> buildSignedBody(String msgType, Object request) {
        JSONObject json = JSON.parseObject(JSON.toJSONString(request));
        String signKey = json.getString("signKey");
        json.remove("signKey");
        json.put("msgType", msgType);
        json.put("requestTimestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        Map<String, Object> body = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                body.put(entry.getKey(), entry.getValue());
            }
        }
        body.put("sign", NotifyVerifier.sign(body, signKey));
        return body;
    }

    private <T> T doRequest(String msgType, Class<T> responseType, Object request) {
        try {
            Map<String, Object> body = buildSignedBody(msgType, request);
            String responseBody = postJson(invoiceProperties().getApiBaseUrl() + msgType, JSON.toJSONString(body));
            JSONObject responseJson = JSON.parseObject(responseBody);
            String resultCode = responseJson.getString("resultCode");
            if (resultCode != null && !"SUCCESS".equals(resultCode)) {
                throw new ServiceException("银联发票错误: " + resultCode + " " + responseJson.getString("resultMsg"), 10201);
            }
            return JSON.parseObject(responseBody, responseType);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("银联发票调用异常: " + e.getMessage(), 10201).setDetailMessage(e.toString());
        }
    }

    private String postJson(String url, String json) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(invoiceProperties().getConnectTimeoutMs()))
                .setResponseTimeout(Timeout.ofMilliseconds(invoiceProperties().getReadTimeoutMs()))
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)));
            return client.execute(post, response -> {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getCode() != 200) {
                    throw new ServiceException("银联发票 HTTP " + response.getCode(), 10201);
                }
                return responseBody;
            });
        }
    }

    private Map<String, String> parseJsonMap(String rawBody) {
        JSONObject json = JSON.parseObject(rawBody);
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() != null) {
                params.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return params;
    }

    private NotifyPayload toPayload(Map<String, String> params) {
        NotifyPayload payload = new NotifyPayload();
        payload.setMerOrderId(params.get("merOrderId"));
        payload.setMerOrderDate(params.get("merOrderDate"));
        payload.setStatus(params.get("status"));
        payload.setInvoiceNo(params.get("invoiceNo"));
        payload.setInvoiceCode(params.get("invoiceCode"));
        payload.setPdfUrl(params.get("pdfUrl"));
        return payload;
    }

    private PiiProperties.Invoice invoiceProperties() {
        PiiProperties props = properties == null ? new PiiProperties() : properties;
        return props.getInvoice();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
