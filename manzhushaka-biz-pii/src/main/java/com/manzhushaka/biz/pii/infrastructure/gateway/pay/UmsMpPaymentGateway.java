package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.chinaums.open.pay.modules.official.OfficialAccountsPayUtil;
import com.chinaums.open.pay.modules.official.request.OfficialAccountOrderParams;
import com.chinaums.open.pay.modules.official.request.OfficialAccountOrderRefundParams;
import com.chinaums.open.pay.modules.official.response.OfficialAccountOrderRefundResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateResponse;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundResponse;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "REAL")
public class UmsMpPaymentGateway implements PaymentGateway {

    @Override
    public PreCreateResponse preCreate(PreCreateRequest request) {
        try {
            OfficialAccountOrderParams params = new OfficialAccountOrderParams();
            params.autoFillParams();
            if (hasText(request.getInstMid())) {
                params.setInstMid(request.getInstMid());
            }
            params.setMerOrderId(request.getOutTradeNo());
            params.setMid(request.getMerchantId());
            params.setTid(request.getTerminalId());
            params.setTotalAmount(BigDecimal.valueOf(request.getTotalAmount()));
            params.setSubOpenId(request.getOpenid());
            params.setNotifyUrl(request.getNotifyUrl());
            params.setOrderDesc(hasText(request.getOrderDesc()) ? request.getOrderDesc() : request.getOutTradeNo());

            String payUrl = OfficialAccountsPayUtil.order(
                    params,
                    request.getAppId(),
                    request.getAppKey(),
                    null,
                    request.isProd()
            );

            PreCreateResponse response = new PreCreateResponse();
            response.setPrepayId(request.getOutTradeNo());
            response.setNonceStr(params.getMsgId());
            response.setTimestamp(params.getRequestTimestamp());
            response.setSignType("UMS");
            response.setPackageStr(payUrl);
            response.setPayUrl(payUrl);
            return response;
        } catch (Exception e) {
            throw new ServiceException("公众号支付预下单失败: " + e.getMessage()).setDetailMessage(e.toString());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            OfficialAccountOrderRefundParams params = new OfficialAccountOrderRefundParams();
            params.autoFillParams();
            if (hasText(request.getInstMid())) {
                params.setInstMid(request.getInstMid());
            }
            params.setMerOrderId(request.getOutTradeNo());
            params.setRefundOrderId(request.getRefundOrderId());
            params.setRefundAmount(BigDecimal.valueOf(request.getRefundAmount()));
            params.setRefundDesc(request.getRefundDesc());
            params.setMid(request.getMerchantId());
            params.setTid(request.getTerminalId());

            OfficialAccountOrderRefundResponse umsResponse = OfficialAccountsPayUtil.refund(
                    params,
                    request.getAppId(),
                    request.getAppKey(),
                    null,
                    request.isProd()
            );

            RefundResponse response = new RefundResponse();
            response.setErrCode(umsResponse.getErrCode());
            response.setErrMsg(umsResponse.getErrMsg());
            response.setTradeNo(firstText(umsResponse.getSeqId(), umsResponse.getTargetOrderId()));
            response.setRefundOrderId(umsResponse.getRefundOrderId());
            response.setRefundStatus(umsResponse.getRefundStatus());
            return response;
        } catch (Exception e) {
            throw new ServiceException("公众号支付退款失败: " + e.getMessage()).setDetailMessage(e.toString());
        }
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
        payload.setOutTradeNo(firstText(params.get("outTradeNo"), params.get("merOrderId")));
        payload.setTradeNo(firstText(params.get("tradeNo"), params.get("seqId"), params.get("refId")));
        payload.setTradeStatus(firstText(params.get("tradeStatus"), params.get("status")));
        payload.setRefundOrderId(params.get("refundOrderId"));
        payload.setTotalAmount(parseLong(params.get("totalAmount")));
        payload.setRefundAmount(parseLong(params.get("refundAmount")));
        return payload;
    }

    private Long parseLong(String value) {
        if (!hasText(value)) {
            return null;
        }
        return new BigDecimal(value).longValue();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
