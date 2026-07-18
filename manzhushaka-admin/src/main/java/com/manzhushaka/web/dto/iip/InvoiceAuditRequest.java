package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发票审核请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class InvoiceAuditRequest
{
    /** 发票ID */
    @NotNull(message = "发票ID不能为空")
    private Long invoiceId;

    /** 审核结果（true 通过，false 驳回） */
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    /** 审核备注（驳回时必填原因） */
    @Size(max = 255, message = "审核备注不能超过255个字符")
    private String auditRemark;

    public Long getInvoiceId()
    {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId)
    {
        this.invoiceId = invoiceId;
    }

    public Boolean getPass()
    {
        return pass;
    }

    public void setPass(Boolean pass)
    {
        this.pass = pass;
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
