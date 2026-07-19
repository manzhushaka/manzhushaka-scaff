package com.manzhushaka.iip.application.result.banner;

import com.manzhushaka.iip.domain.IipBanner;

/**
 * 小程序轮播图条目结果（仅暴露 C 端轮播所需字段，不输出审计信息）。
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
public record MiniappBannerResult(Long bannerId, String title, String imageUrl, String linkType,
        String linkValue, Integer sort)
{
    /**
     * 从领域实体转换为 C 端结果。
     *
     * @param banner 轮播图实体
     * @return C 端轮播图条目
     */
    public static MiniappBannerResult from(IipBanner banner)
    {
        return new MiniappBannerResult(banner.getBannerId(), banner.getTitle(), banner.getImageUrl(),
                banner.getLinkType(), banner.getLinkValue(), banner.getSort());
    }

    @Override
    public String toString()
    {
        return "MiniappBannerResult[bannerId=" + bannerId + ", title=" + title + ", sort=" + sort + "]";
    }
}
