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
 * 活动对象 iip_activity
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 活动编号（A+年月+4位序号） */
    private String activityNo;

    /** 活动名称 */
    private String activityName;

    /** 活动封面 */
    private String coverImage;

    /** 活动描述 */
    private String description;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 发票金额积分比例 */
    private BigDecimal pointsRatio;

    /** 参与商户数上限（-1不限） */
    private Integer merchantLimit;

    /** 发券总额度（-1不限） */
    private Integer couponQuota;

    /** 适用市县（空=全省通用） */
    private String city;

    /** 地域类型（province全省 city市县 business_district商圈 scenic景区） */
    private String regionType;

    /** 商圈/景区名称（regionType为商圈/景区时填） */
    private String regionName;

    /** 优先级（数值越大越优先） */
    private Integer priority;

    /** 状态（0启用 1停用） */
    private String status;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    @NotBlank(message = "活动编号不能为空")
    @Size(min = 0, max = 32, message = "活动编号不能超过32个字符")
    public String getActivityNo()
    {
        return activityNo;
    }

    public void setActivityNo(String activityNo)
    {
        this.activityNo = activityNo;
    }

    @NotBlank(message = "活动名称不能为空")
    @Size(min = 0, max = 128, message = "活动名称不能超过128个字符")
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

    @NotNull(message = "开始时间不能为空")
    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    @NotNull(message = "结束时间不能为空")
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

    @Size(min = 0, max = 64, message = "适用市县不能超过64个字符")
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    @Size(min = 0, max = 20, message = "地域类型不能超过20个字符")
    public String getRegionType()
    {
        return regionType;
    }

    public void setRegionType(String regionType)
    {
        this.regionType = regionType;
    }

    @Size(min = 0, max = 128, message = "商圈/景区名称不能超过128个字符")
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

    @NotBlank(message = "状态不能为空")
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("activityId", getActivityId())
            .append("activityNo", getActivityNo())
            .append("activityName", getActivityName())
            .append("coverImage", getCoverImage())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("pointsRatio", getPointsRatio())
            .append("merchantLimit", getMerchantLimit())
            .append("couponQuota", getCouponQuota())
            .append("city", getCity())
            .append("regionType", getRegionType())
            .append("regionName", getRegionName())
            .append("priority", getPriority())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
