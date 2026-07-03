package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotNull;

public class QrcodeTaxItemRequest {
    @NotNull(message = "税目ID不能为空")
    private Long taxItemId;
    private Long defaultAmount;

    public Long getTaxItemId() { return taxItemId; }
    public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
    public Long getDefaultAmount() { return defaultAmount; }
    public void setDefaultAmount(Long defaultAmount) { this.defaultAmount = defaultAmount; }
}
