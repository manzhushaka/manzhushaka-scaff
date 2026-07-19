package com.manzhushaka.iip.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipBanner;
import com.manzhushaka.iip.mapper.IipBannerMapper;
import com.manzhushaka.iip.service.IIipBannerService;

/**
 * 轮播图 服务层实现
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
@Service
public class IipBannerServiceImpl implements IIipBannerService
{
    /** 启用状态 */
    private static final String STATUS_ENABLED = "0";

    /** 跳转类型：不跳转 */
    private static final String LINK_TYPE_NONE = "none";

    /** 默认排序 */
    private static final int DEFAULT_SORT = 0;

    @Autowired
    private IipBannerMapper bannerMapper;

    /**
     * 通过ID查询轮播图
     *
     * @param bannerId 轮播图ID
     * @return 轮播图信息，不存在时返回null
     */
    @Override
    public IipBanner selectIipBannerById(Long bannerId)
    {
        return bannerMapper.selectIipBannerById(bannerId);
    }

    /**
     * 查询轮播图列表
     *
     * @param iipBanner 查询条件
     * @return 轮播图集合
     */
    @Override
    public List<IipBanner> selectIipBannerList(IipBanner iipBanner)
    {
        return bannerMapper.selectIipBannerList(iipBanner);
    }

    /**
     * 查询启用中的轮播图列表（按排序升序，供小程序首页轮播使用）
     *
     * @return 启用中的轮播图集合，无启用轮播图时返回空集合
     */
    @Override
    public List<IipBanner> selectEnabledIipBannerList()
    {
        return bannerMapper.selectEnabledIipBannerList();
    }

    /**
     * 新增轮播图（跳转类型默认none，排序默认0，状态默认启用）
     *
     * @param iipBanner 轮播图信息
     * @return 结果
     */
    @Override
    public int insertIipBanner(IipBanner iipBanner)
    {
        fillBannerDefaults(iipBanner);
        return bannerMapper.insertIipBanner(iipBanner);
    }

    /**
     * 修改轮播图
     *
     * @param iipBanner 轮播图信息
     * @return 结果
     */
    @Override
    public int updateIipBanner(IipBanner iipBanner)
    {
        fillBannerDefaults(iipBanner);
        return bannerMapper.updateIipBanner(iipBanner);
    }

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 需要删除的轮播图ID
     * @return 结果
     */
    @Override
    public int deleteIipBannerByIds(Long[] bannerIds)
    {
        return bannerMapper.deleteIipBannerByIds(bannerIds);
    }

    /**
     * 填充轮播图默认值（跳转类型默认none，排序默认0，状态默认启用）
     *
     * @param iipBanner 轮播图信息
     */
    private void fillBannerDefaults(IipBanner iipBanner)
    {
        if (StringUtils.isEmpty(iipBanner.getLinkType()))
        {
            iipBanner.setLinkType(LINK_TYPE_NONE);
        }
        if (iipBanner.getSort() == null)
        {
            iipBanner.setSort(DEFAULT_SORT);
        }
        if (StringUtils.isEmpty(iipBanner.getStatus()))
        {
            iipBanner.setStatus(STATUS_ENABLED);
        }
    }
}
