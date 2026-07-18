package com.manzhushaka.iip.application.merchant.command;

import java.math.BigDecimal;

/**
 * 商户保存命令（管理端新增/修改共用）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveMerchantCommand(Long merchantId, String merchantName, String category, String city,
        String contactName, String contactPhone, String address, String description, String logo,
        String businessHours, BigDecimal longitude, BigDecimal latitude,
        String businessLicense, Long memberId, String status, String remark)
{
    @Override
    public String toString()
    {
        return "SaveMerchantCommand[merchantId=" + merchantId + ", merchantName=" + merchantName
                + ", category=" + category + ", memberId=" + memberId + ", status=" + status + "]";
    }
}
