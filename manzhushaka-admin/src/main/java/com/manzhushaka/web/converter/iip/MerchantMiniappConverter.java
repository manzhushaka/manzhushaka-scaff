package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.merchant.command.MerchantApplyCommand;
import com.manzhushaka.iip.application.merchant.command.MerchantVerifyCommand;
import com.manzhushaka.web.dto.miniapp.MerchantApplyRequest;
import com.manzhushaka.web.dto.miniapp.MerchantVerifyRequest;

/**
 * 商户 Web 模型转换器（小程序端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class MerchantMiniappConverter
{
    private MerchantMiniappConverter()
    {
    }

    /**
     * 转换为商户入驻申请命令
     *
     * @param request 入驻申请请求
     * @return 入驻申请命令
     */
    public static MerchantApplyCommand toApplyCommand(MerchantApplyRequest request)
    {
        return new MerchantApplyCommand(request.getMerchantName(), request.getCategory(), request.getContactName(),
                request.getContactPhone(), request.getAddress(), request.getBusinessLicense());
    }

    /**
     * 转换为商户核销命令
     *
     * @param request 核销请求
     * @return 核销命令
     */
    public static MerchantVerifyCommand toVerifyCommand(MerchantVerifyRequest request)
    {
        return new MerchantVerifyCommand(request.getVerifyCode());
    }
}
