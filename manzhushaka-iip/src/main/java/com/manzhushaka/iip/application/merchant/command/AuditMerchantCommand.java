package com.manzhushaka.iip.application.merchant.command;

/**
 * 商户审核命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record AuditMerchantCommand(Long merchantId, Boolean approve, String auditRemark)
{
    @Override
    public String toString()
    {
        return "AuditMerchantCommand[merchantId=" + merchantId + ", approve=" + approve + "]";
    }
}
