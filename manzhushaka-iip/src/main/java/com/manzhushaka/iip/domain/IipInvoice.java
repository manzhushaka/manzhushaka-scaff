package com.manzhushaka.iip.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 发票对象 iip_invoice
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipInvoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 发票ID */
    private Long invoiceId;

    /** 上传用户ID */
    private Long memberId;

    /** 关联商户ID */
    private Long merchantId;

    /** 商户名称 */
    private String merchantName;

    /** 发票代码 */
    private String invoiceCode;

    /** 发票号码 */
    private String invoiceNo;

    /** 开票日期 */
    private Date invoiceDate;

    /** 发票金额 */
    private BigDecimal amount;

    /** 发票图片地址 */
    private String imageUrl;

    /** 状态（0待审核 1已通过 2已驳回） */
    private String status;

    /** 发放积分数 */
    private Integer points;

    /** 发分依据的活动ID */
    private Long activityId;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 审核备注（驳回填原因） */
    private String auditRemark;

    public Long getInvoiceId()
    {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId)
    {
        this.invoiceId = invoiceId;
    }

    @NotNull(message = "上传用户ID不能为空")
    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    @NotBlank(message = "商户名称不能为空")
    @Size(min = 0, max = 128, message = "商户名称不能超过128个字符")
    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    @Size(min = 0, max = 20, message = "发票代码不能超过20个字符")
    public String getInvoiceCode()
    {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode)
    {
        this.invoiceCode = invoiceCode;
    }

    @NotBlank(message = "发票号码不能为空")
    @Size(min = 0, max = 30, message = "发票号码不能超过30个字符")
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

    @NotNull(message = "发票金额不能为空")
    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    @NotBlank(message = "发票图片地址不能为空")
    @Size(min = 0, max = 255, message = "发票图片地址不能超过255个字符")
    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    @NotBlank(message = "状态不能为空")
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("invoiceId", getInvoiceId())
            .append("memberId", getMemberId())
            .append("merchantId", getMerchantId())
            .append("merchantName", getMerchantName())
            .append("invoiceCode", getInvoiceCode())
            .append("invoiceNo", getInvoiceNo())
            .append("invoiceDate", getInvoiceDate())
            .append("amount", getAmount())
            .append("imageUrl", getImageUrl())
            .append("status", getStatus())
            .append("points", getPoints())
            .append("activityId", getActivityId())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("auditRemark", getAuditRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
