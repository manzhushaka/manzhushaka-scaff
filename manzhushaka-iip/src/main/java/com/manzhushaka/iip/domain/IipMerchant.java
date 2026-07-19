package com.manzhushaka.iip.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 商户对象 iip_merchant
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipMerchant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商户ID */
    private Long merchantId;

    /** 商户编号（M+年月+5位序号） */
    private String merchantNo;

    /** 商户名称 */
    private String merchantName;

    /** 商户类别（餐饮/住宿/加油/景区等） */
    private String category;

    /** 所在市县（用于活动地域匹配） */
    private String city;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 商户地址 */
    private String address;

    /** 商家介绍 */
    private String description;

    /** 商户logo图片 */
    private String logo;

    /** 营业时间 */
    private String businessHours;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 营业执照图片 */
    private String businessLicense;

    /** 绑定的登录用户ID */
    private Long memberId;

    /** 状态（0正常 1停用 2待审核） */
    private String status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 是否推荐（0推荐 1不推荐） */
    private String isRecommend;

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    @NotBlank(message = "商户编号不能为空")
    @Size(min = 0, max = 32, message = "商户编号不能超过32个字符")
    public String getMerchantNo()
    {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo)
    {
        this.merchantNo = merchantNo;
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

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    @Size(min = 0, max = 64, message = "所在市县不能超过64个字符")
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    @Size(min = 0, max = 20, message = "联系电话不能超过20个字符")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    @Size(min = 0, max = 64, message = "营业时间不能超过64个字符")
    public String getBusinessHours()
    {
        return businessHours;
    }

    public void setBusinessHours(String businessHours)
    {
        this.businessHours = businessHours;
    }

    public BigDecimal getLongitude()
    {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude()
    {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
    }

    public String getBusinessLicense()
    {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense)
    {
        this.businessLicense = businessLicense;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
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

    public String getIsRecommend()
    {
        return isRecommend;
    }

    public void setIsRecommend(String isRecommend)
    {
        this.isRecommend = isRecommend;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("merchantId", getMerchantId())
            .append("merchantNo", getMerchantNo())
            .append("merchantName", getMerchantName())
            .append("category", getCategory())
            .append("city", getCity())
            .append("contactName", getContactName())
            .append("address", getAddress())
            .append("description", getDescription())
            .append("logo", getLogo())
            .append("businessHours", getBusinessHours())
            .append("memberId", getMemberId())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("auditRemark", getAuditRemark())
            .append("isRecommend", getIsRecommend())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
