package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateRefundRequest {
    @NotNull(message = "商户不能为空")
    private Long merchantId;

    @NotNull(message = "订单不能为空")
    private Long payOrderId;

    @NotNull(message = "退款金额不能为空")
    @Min(value = 1, message = "退款金额必须大于0")
    private Long amount;

    private String reason;

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getPayOrderId() { return payOrderId; }
    public void setPayOrderId(Long payOrderId) { this.payOrderId = payOrderId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
