package com.manzhushaka.iip.application.merchant.command;

/**
 * 小程序商户入驻申请命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MerchantApplyCommand(String merchantName, String category, String contactName, String contactPhone,
        String address, String businessLicense)
{
    @Override
    public String toString()
    {
        return "MerchantApplyCommand[merchantName=" + merchantName + ", category=" + category + "]";
    }
}
