package com.manzhushaka.web.dto.iip;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 活动请求（列表查询与新增/修改共用，活动编号由服务端生成仅用于查询）。
 * 列表时间范围通过 params[beginTime]/params[endTime] 传递，
 * 活动起止时间为 startTime/endTime（JSON 格式 yyyy-MM-dd HH:mm:ss）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class ActivityRequest
{
    /** 请求参数（列表查询时间范围 beginTime/endTime） */
    private Map<String, Object> params = new HashMap<>();

    private Long activityId;

    @Size(max = 32, message = "活动编号不能超过32个字符")
    private String activityNo;

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 128, message = "活动名称不能超过128个字符")
    private String activityName;

    @Size(max = 255, message = "活动封面不能超过255个字符")
    private String coverImage;

    private String description;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @NotNull(message = "积分比例不能为空")
    private BigDecimal pointsRatio;

    private Integer merchantLimit;

    private Integer couponQuota;

    /** 适用市县（空=全省通用） */
    @Size(max = 64, message = "适用市县不能超过64个字符")
    private String city;

    /** 地域类型（province全省 city市县 business_district商圈 scenic景区） */
    @Size(max = 20, message = "地域类型不能超过20个字符")
    private String regionType;

    /** 商圈/景区名称（regionType为商圈/景区时必填） */
    @Size(max = 128, message = "商圈/景区名称不能超过128个字符")
    private String regionName;

    /** 优先级（数值越大越优先） */
    private Integer priority;

    private String status;

    private String remark;

    /** 单张发票积分上限（-1不限） */
    private Integer singleInvoiceCap;

    /** 活动内每人每月积分上限（-1不限） */
    private Integer monthlyMemberCap;

    /** 商户积分范围（all全部 whitelist活动商户白名单） */
    @Size(max = 16, message = "商户积分范围不能超过16个字符")
    private String merchantScope;

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params == null ? new HashMap<>() : params;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public String getActivityNo()
    {
        return activityNo;
    }

    public void setActivityNo(String activityNo)
    {
        this.activityNo = activityNo;
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName)
    {
        this.activityName = activityName;
    }

    public String getCoverImage()
    {
        return coverImage;
    }

    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public BigDecimal getPointsRatio()
    {
        return pointsRatio;
    }

    public void setPointsRatio(BigDecimal pointsRatio)
    {
        this.pointsRatio = pointsRatio;
    }

    public Integer getMerchantLimit()
    {
        return merchantLimit;
    }

    public void setMerchantLimit(Integer merchantLimit)
    {
        this.merchantLimit = merchantLimit;
    }

    public Integer getCouponQuota()
    {
        return couponQuota;
    }

    public void setCouponQuota(Integer couponQuota)
    {
        this.couponQuota = couponQuota;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getRegionType()
    {
        return regionType;
    }

    public void setRegionType(String regionType)
    {
        this.regionType = regionType;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
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

    public Integer getSingleInvoiceCap()
    {
        return singleInvoiceCap;
    }

    public void setSingleInvoiceCap(Integer singleInvoiceCap)
    {
        this.singleInvoiceCap = singleInvoiceCap;
    }

    public Integer getMonthlyMemberCap()
    {
        return monthlyMemberCap;
    }

    public void setMonthlyMemberCap(Integer monthlyMemberCap)
    {
        this.monthlyMemberCap = monthlyMemberCap;
    }

    public String getMerchantScope()
    {
        return merchantScope;
    }

    public void setMerchantScope(String merchantScope)
    {
        this.merchantScope = merchantScope;
    }
}
