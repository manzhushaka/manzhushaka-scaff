package com.manzhushaka.web.dto.miniapp;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 小程序提交发票请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class InvoiceSubmitRequest
{
    /** 关联商户ID（手输商户名时可为空） */
    private Long merchantId;

    /** 商户名称 */
    @NotBlank(message = "商户名称不能为空")
    @Size(max = 128, message = "商户名称不能超过128个字符")
    private String merchantName;

    /** 发票代码 */
    @Size(max = 20, message = "发票代码不能超过20个字符")
    private String invoiceCode;

    /** 发票号码 */
    @NotBlank(message = "发票号码不能为空")
    @Size(max = 30, message = "发票号码不能超过30个字符")
    private String invoiceNo;

    /** 开票日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 发票金额 */
    @NotNull(message = "发票金额不能为空")
    private BigDecimal amount;

    /** 发票图片地址 */
    @NotBlank(message = "发票图片地址不能为空")
    @Size(max = 255, message = "发票图片地址不能超过255个字符")
    private String imageUrl;

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getInvoiceCode()
    {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode)
    {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceNo()
    {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo)
    {
        this.invoiceNo = invoiceNo;
    }

    public Date getInvoiceDate()
    {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate)
    {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
}
