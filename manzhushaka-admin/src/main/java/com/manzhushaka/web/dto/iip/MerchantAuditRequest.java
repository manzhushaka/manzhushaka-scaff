package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商户审核请求（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MerchantAuditRequest
{
    /** 商户ID */
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    /** 审核结论：true 通过，false 驳回 */
    @NotNull(message = "审核结论不能为空")
    private Boolean approve;

    /** 审核备注（驳回时必填） */
    @Size(max = 255, message = "审核备注不能超过255个字符")
    private String auditRemark;

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Boolean getApprove()
    {
        return approve;
    }

    public void setApprove(Boolean approve)
    {
        this.approve = approve;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }
}
