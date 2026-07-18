package com.manzhushaka.iip.application.merchant.result;

import java.math.BigDecimal;
import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 商户结果（管理端展示与导出）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MerchantResult(
        @Excel(name = "商户ID", cellType = ColumnType.NUMERIC) Long merchantId,
        @Excel(name = "商户编号") String merchantNo,
        @Excel(name = "商户名称") String merchantName,
        @Excel(name = "商户类别") String category,
        @Excel(name = "所在市县") String city,
        @Excel(name = "联系人") String contactName,
        @Excel(name = "联系电话") String contactPhone,
        @Excel(name = "商户地址") String address,
        String description,
        String logo,
        String businessHours,
        BigDecimal longitude,
        BigDecimal latitude,
        String businessLicense,
        @Excel(name = "绑定用户ID", cellType = ColumnType.NUMERIC) Long memberId,
        @Excel(name = "状态", readConverterExp = "0=正常,1=停用,2=待审核") String status,
        @Excel(name = "审核人") String auditBy,
        @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date auditTime,
        @Excel(name = "审核备注") String auditRemark,
        String createBy,
        @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date createTime,
        String updateBy, Date updateTime, String remark)
{
    @Override
    public String toString()
    {
        return "MerchantResult[merchantId=" + merchantId + ", merchantNo=" + merchantNo
                + ", merchantName=" + merchantName + ", status=" + status + "]";
    }
}
