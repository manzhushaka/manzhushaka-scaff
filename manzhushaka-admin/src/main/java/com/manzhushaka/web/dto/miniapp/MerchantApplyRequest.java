package com.manzhushaka.web.dto.miniapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 小程序商户入驻申请请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MerchantApplyRequest
{
    /** 商户名称 */
    @NotBlank(message = "商户名称不能为空")
    @Size(max = 128, message = "商户名称不能超过128个字符")
    private String merchantName;

    /** 商户类别（餐饮/住宿/加油/景区等） */
    @NotBlank(message = "商户类别不能为空")
    @Size(max = 64, message = "商户类别不能超过64个字符")
    private String category;

    /** 联系人 */
    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人不能超过64个字符")
    private String contactName;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String contactPhone;

    /** 商户地址 */
    @NotBlank(message = "商户地址不能为空")
    @Size(max = 255, message = "商户地址不能超过255个字符")
    private String address;

    /** 营业执照图片 */
    @NotBlank(message = "营业执照不能为空")
    @Size(max = 255, message = "营业执照不能超过255个字符")
    private String businessLicense;

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

    public String getBusinessLicense()
    {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense)
    {
        this.businessLicense = businessLicense;
    }
}
