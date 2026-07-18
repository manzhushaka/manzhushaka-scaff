package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.Size;
import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 发票查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class InvoiceRequest extends DateRangeRequest
{
    /** 状态（0待审核 1已通过 2已驳回） */
    private String status;

    /** 发票号码 */
    @Size(max = 30, message = "发票号码不能超过30个字符")
    private String invoiceNo;

    /** 商户名称（模糊查询） */
    @Size(max = 128, message = "商户名称不能超过128个字符")
    private String merchantName;

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getInvoiceNo()
    {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo)
    {
        this.invoiceNo = invoiceNo;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }
}
