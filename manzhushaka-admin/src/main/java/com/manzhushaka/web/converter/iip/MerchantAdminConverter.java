package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.merchant.command.AuditMerchantCommand;
import com.manzhushaka.iip.application.merchant.command.SaveMerchantCommand;
import com.manzhushaka.iip.application.merchant.query.MerchantQuery;
import com.manzhushaka.web.dto.iip.MerchantAuditRequest;
import com.manzhushaka.web.dto.iip.MerchantRequest;

/**
 * 商户 Web 模型转换器（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class MerchantAdminConverter
{
    private MerchantAdminConverter()
    {
    }

    /**
     * 转换为商户查询条件
     *
     * @param request 商户请求
     * @return 商户查询条件
     */
    public static MerchantQuery toQuery(MerchantRequest request)
    {
        return new MerchantQuery(request.getMerchantNo(), request.getMerchantName(), request.getCategory(),
                request.getStatus(), request.getBeginTime(), request.getEndTime());
    }

    /**
     * 转换为商户保存命令
     *
     * @param request 商户请求
     * @return 商户保存命令
     */
    public static SaveMerchantCommand toCommand(MerchantRequest request)
    {
        return new SaveMerchantCommand(request.getMerchantId(), request.getMerchantName(), request.getCategory(),
                request.getCity(), request.getContactName(), request.getContactPhone(), request.getAddress(),
                request.getDescription(), request.getLogo(), request.getBusinessHours(), request.getLongitude(),
                request.getLatitude(), request.getBusinessLicense(), request.getMemberId(),
                request.getStatus(), request.getRemark());
    }

    /**
     * 转换为商户审核命令
     *
     * @param request 商户审核请求
     * @return 商户审核命令
     */
    public static AuditMerchantCommand toAuditCommand(MerchantAuditRequest request)
    {
        return new AuditMerchantCommand(request.getMerchantId(), request.getApprove(), request.getAuditRemark());
    }
}
