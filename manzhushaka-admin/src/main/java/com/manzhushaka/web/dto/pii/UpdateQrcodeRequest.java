package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotNull;

public class UpdateQrcodeRequest extends CreateQrcodeRequest {
    @NotNull(message = "二维码ID不能为空")
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
