package com.manzhushaka.iip.application.exchange.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 兑换记录结果（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ExchangeRecordResult(
        @Excel(name = "记录ID", cellType = ColumnType.NUMERIC) Long recordId,
        Long couponId,
        @Excel(name = "券名称") String couponName,
        @Excel(name = "券类型", readConverterExp = "ticket=门票,virtual=虚拟物品,full_reduction=满减券,discount=折扣券") String couponType,
        @Excel(name = "用户ID", cellType = ColumnType.NUMERIC) Long memberId,
        @Excel(name = "消耗积分", cellType = ColumnType.NUMERIC) Integer pointsCost,
        @Excel(name = "核销码") String verifyCode,
        @Excel(name = "状态", readConverterExp = "0=未使用,1=已使用,2=已过期") String status,
        @Excel(name = "兑换时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeTime,
        @Excel(name = "有效期开始", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validStartTime,
        @Excel(name = "有效期结束", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validEndTime,
        @Excel(name = "核销时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date verifyTime,
        @Excel(name = "核销商户ID", cellType = ColumnType.NUMERIC) Long verifyMerchantId,
        @Excel(name = "核销操作人") String verifyBy,
        @Excel(name = "来源活动ID", cellType = ColumnType.NUMERIC) Long activityId,
        String remark)
{
    @Override
    public String toString()
    {
        return "ExchangeRecordResult[recordId=" + recordId + ", couponName=" + couponName
                + ", memberId=" + memberId + ", status=" + status + "]";
    }
}
