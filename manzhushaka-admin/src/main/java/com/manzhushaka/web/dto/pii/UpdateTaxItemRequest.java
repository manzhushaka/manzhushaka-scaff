package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotNull;

public class UpdateTaxItemRequest extends CreateTaxItemRequest {
    @NotNull(message = "税目ID不能为空")
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
