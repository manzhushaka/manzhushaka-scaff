package com.manzhushaka.iip.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 轮播图对象 iip_banner
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
public class IipBanner extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 轮播图ID */
    private Long bannerId;

    /** 关联活动ID */
    private Long activityId;

    /** 轮播图标题 */
    private String title;

    /** 轮播图图片地址 */
    private String imageUrl;

    /** 跳转类型（none不跳转） */
    private String linkType;

    /** 跳转目标（linkType为none时为空） */
    private String linkValue;

    /** 排序（数值越小越靠前） */
    private Integer sort;

    /** 状态（0启用 1停用） */
    private String status;

    public Long getBannerId()
    {
        return bannerId;
    }

    public void setBannerId(Long bannerId)
    {
        this.bannerId = bannerId;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    @NotBlank(message = "轮播图标题不能为空")
    @Size(min = 0, max = 128, message = "轮播图标题不能超过128个字符")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @NotBlank(message = "轮播图图片不能为空")
    @Size(min = 0, max = 255, message = "轮播图图片地址不能超过255个字符")
    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    @Size(min = 0, max = 16, message = "跳转类型不能超过16个字符")
    public String getLinkType()
    {
        return linkType;
    }

    public void setLinkType(String linkType)
    {
        this.linkType = linkType;
    }

    @Size(min = 0, max = 255, message = "跳转目标不能超过255个字符")
    public String getLinkValue()
    {
        return linkValue;
    }

    public void setLinkValue(String linkValue)
    {
        this.linkValue = linkValue;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

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
            .append("bannerId", getBannerId())
            .append("activityId", getActivityId())
            .append("title", getTitle())
            .append("imageUrl", getImageUrl())
            .append("linkType", getLinkType())
            .append("linkValue", getLinkValue())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
