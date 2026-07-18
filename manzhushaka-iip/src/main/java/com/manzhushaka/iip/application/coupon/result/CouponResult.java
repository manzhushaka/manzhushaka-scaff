package com.manzhushaka.iip.application.coupon.result;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 券定义结果（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CouponResult(
        @Excel(name = "券ID", cellType = ColumnType.NUMERIC) Long couponId,
        @Excel(name = "券名称") String couponName,
        @Excel(name = "券类型", readConverterExp = "ticket=门票,virtual=虚拟物品,full_reduction=满减券,discount=折扣券") String couponType,
        String coverImage,
        @Excel(name = "适用对象") String targetName,
        @Excel(name = "兑换积分", cellType = ColumnType.NUMERIC) Integer pointsCost,
        @Excel(name = "总库存", cellType = ColumnType.NUMERIC) Integer totalStock,
        @Excel(name = "剩余库存", cellType = ColumnType.NUMERIC) Integer remainStock,
        @Excel(name = "每人限兑", cellType = ColumnType.NUMERIC) Integer perMemberLimit,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeEndTime,
        @Excel(name = "有效期类型", readConverterExp = "fixed=固定区间,days=领取后N天") String validType,
        @Excel(name = "有效期开始", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validStartTime,
        @Excel(name = "有效期结束", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validEndTime,
        @Excel(name = "有效天数", cellType = ColumnType.NUMERIC) Integer validDays,
        @Excel(name = "满减门槛") BigDecimal thresholdAmount,
        @Excel(name = "满减面额") BigDecimal discountAmount,
        Long merchantId,
        String useDesc,
        @Excel(name = "状态", readConverterExp = "0=上架,1=下架") String status,
        @Excel(name = "显示顺序", cellType = ColumnType.NUMERIC) Integer sort,
        @Excel(name = "券品类", readConverterExp = "general=通用,scenic_ticket=景区门票,hotel=酒店券,dining=餐饮券,flight_package=机票+权益包,duty_free=免税周边") String category,
        @Excel(name = "赞助方类型", readConverterExp = "platform=平台,bank=银行,merchant=商户") String sponsorType,
        @Excel(name = "赞助方名称") String sponsorName,
        String createBy,
        @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date createTime,
        String updateBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date updateTime,
        String remark)
{
    @Override
    public String toString()
    {
        return "CouponResult[couponId=" + couponId + ", couponName=" + couponName
                + ", couponType=" + couponType + ", status=" + status + "]";
    }
}
