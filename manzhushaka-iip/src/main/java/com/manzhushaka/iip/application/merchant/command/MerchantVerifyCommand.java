package com.manzhushaka.iip.application.merchant.command;

/**
 * 小程序商户核销命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MerchantVerifyCommand(String verifyCode)
{
    @Override
    public String toString()
    {
        return "MerchantVerifyCommand[verifyCode=" + verifyCode + "]";
    }
}
