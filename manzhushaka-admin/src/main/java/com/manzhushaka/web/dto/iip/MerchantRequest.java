package com.manzhushaka.web.dto.iip;

import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 商户请求（管理端查询、新增、修改共用；查询场景不触发必填校验）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MerchantRequest extends DateRangeRequest
{
    /** 商户ID（修改时必填） */
    private Long merchantId;

    /** 商户编号（精确查询） */
    @Size(max = 32, message = "商户编号不能超过32个字符")
    private String merchantNo;

    /** 商户名称（模糊查询） */
    @NotBlank(message = "商户名称不能为空")
    @Size(max = 128, message = "商户名称不能超过128个字符")
    private String merchantName;

    /** 商户类别 */
    @Size(max = 64, message = "商户类别不能超过64个字符")
    private String category;

    /** 所在市县（用于活动地域匹配） */
    @Size(max = 64, message = "所在市县不能超过64个字符")
    private String city;

    /** 联系人 */
    @Size(max = 64, message = "联系人不能超过64个字符")
    private String contactName;

    /** 联系电话 */
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String contactPhone;

    /** 商户地址 */
    @Size(max = 255, message = "商户地址不能超过255个字符")
    private String address;

    /** 商家介绍 */
    @Size(max = 500, message = "商家介绍不能超过500个字符")
    private String description;

    /** 商户logo图片 */
    @Size(max = 255, message = "商户logo图片不能超过255个字符")
    private String logo;

    /** 营业时间 */
    @Size(max = 64, message = "营业时间不能超过64个字符")
    private String businessHours;

    /** 经度 */
    @Digits(integer = 4, fraction = 6, message = "经度格式不正确，最多4位整数和6位小数")
    private BigDecimal longitude;

    /** 纬度 */
    @Digits(integer = 4, fraction = 6, message = "纬度格式不正确，最多4位整数和6位小数")
    private BigDecimal latitude;

    /** 营业执照图片 */
    @Size(max = 255, message = "营业执照不能超过255个字符")
    private String businessLicense;

    /** 绑定的登录用户ID */
    private Long memberId;

    /** 状态（0正常 1停用 2待审核） */
    private String status;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getMerchantNo()
    {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo)
    {
        this.merchantNo = merchantNo;
    }

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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
