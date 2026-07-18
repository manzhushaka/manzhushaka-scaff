package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 积分手工调整请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class PointsAdjustRequest
{
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @NotNull(message = "调整积分不能为空")
    @Min(value = -1000000, message = "单次扣减积分不能超过1000000")
    @Max(value = 1000000, message = "单次增加积分不能超过1000000")
    private Integer points;

    @NotBlank(message = "调整备注不能为空")
    @Size(max = 255, message = "调整备注不能超过255个字符")
    private String remark;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
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
