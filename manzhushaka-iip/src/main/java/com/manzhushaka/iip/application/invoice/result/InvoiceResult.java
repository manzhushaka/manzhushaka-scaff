package com.manzhushaka.iip.application.invoice.result;

import java.math.BigDecimal;
import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 发票结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record InvoiceResult(
        @Excel(name = "发票编号", cellType = ColumnType.NUMERIC) Long invoiceId,
        Long memberId,
        Long merchantId,
        @Excel(name = "商户名称") String merchantName,
        @Excel(name = "发票代码") String invoiceCode,
        @Excel(name = "发票号码") String invoiceNo,
        @Excel(name = "开票日期", dateFormat = "yyyy-MM-dd") Date invoiceDate,
        @Excel(name = "发票金额") BigDecimal amount,
        String imageUrl,
        @Excel(name = "状态", readConverterExp = "0=待审核,1=已通过,2=已驳回") String status,
        @Excel(name = "发放积分", cellType = ColumnType.NUMERIC) Integer points,
        Long activityId,
        Long pointsRuleId,
        BigDecimal pointsRatioSnapshot,
        String pointsRuleSnapshot,
        @Excel(name = "审核人") String auditBy,
        @Excel(name = "审核时间", dateFormat = "yyyy-MM-dd HH:mm:ss") Date auditTime,
        @Excel(name = "审核备注") String auditRemark,
        @Excel(name = "上传时间", dateFormat = "yyyy-MM-dd HH:mm:ss") Date createTime,
        String remark)
{
    @Override
    public String toString()
    {
        return "InvoiceResult[invoiceId=" + invoiceId + ", memberId=" + memberId
                + ", invoiceNo=" + invoiceNo + ", status=" + status + "]";
    }
}
