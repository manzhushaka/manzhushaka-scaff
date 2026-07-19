package com.manzhushaka.iip.application.result.merchant;

import com.manzhushaka.iip.domain.IipMerchant;

/**
 * 小程序推荐商户条目结果（仅暴露 C 端展示所需字段，不输出联系方式、绑定用户与审计信息）。
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
public record MiniappMerchantRecommendResult(Long merchantId, String merchantName, String category, String city,
        String logo, String description, String businessHours, String address)
{
    /**
     * 从领域实体转换为 C 端结果。
     *
     * @param merchant 商户实体
     * @return C 端推荐商户条目
     */
    public static MiniappMerchantRecommendResult from(IipMerchant merchant)
    {
        return new MiniappMerchantRecommendResult(merchant.getMerchantId(), merchant.getMerchantName(),
                merchant.getCategory(), merchant.getCity(), merchant.getLogo(), merchant.getDescription(),
                merchant.getBusinessHours(), merchant.getAddress());
    }

    @Override
    public String toString()
    {
        return "MiniappMerchantRecommendResult[merchantId=" + merchantId + ", merchantName=" + merchantName
                + ", category=" + category + ", city=" + city + "]";
    }
}
