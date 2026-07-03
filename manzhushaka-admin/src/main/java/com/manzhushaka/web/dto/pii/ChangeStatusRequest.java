package com.manzhushaka.web.dto.pii;

import jakarta.validation.constraints.NotNull;

public class ChangeStatusRequest {
    @NotNull(message = "ID不能为空")
    private Long id;
    @NotNull(message = "状态不能为空")
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
