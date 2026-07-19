package com.manzhushaka.iip.mapper;

import java.util.List;
import com.manzhushaka.iip.domain.IipBanner;

/**
 * 轮播图 数据层
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
public interface IipBannerMapper
{
    /**
     * 通过ID查询轮播图
     *
     * @param bannerId 轮播图ID
     * @return 轮播图信息
     */
    public IipBanner selectIipBannerById(Long bannerId);

    /**
     * 查询轮播图列表
     *
     * @param iipBanner 查询条件
     * @return 轮播图集合
     */
    public List<IipBanner> selectIipBannerList(IipBanner iipBanner);

    /**
     * 查询启用中的轮播图列表（按排序升序）
     *
     * @return 启用中的轮播图集合，无启用轮播图时返回空集合
     */
    public List<IipBanner> selectEnabledIipBannerList();

    /**
     * 新增轮播图
     *
     * @param iipBanner 轮播图信息
     * @return 结果
     */
    public int insertIipBanner(IipBanner iipBanner);

    /**
     * 修改轮播图
     *
     * @param iipBanner 轮播图信息
     * @return 结果
     */
    public int updateIipBanner(IipBanner iipBanner);

    /**
     * 通过ID删除轮播图
     *
     * @param bannerId 轮播图ID
     * @return 结果
     */
    public int deleteIipBannerById(Long bannerId);

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 需要删除的轮播图ID
     * @return 结果
     */
    public int deleteIipBannerByIds(Long[] bannerIds);
}
