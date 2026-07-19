package com.manzhushaka.web.controller.miniapp;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.iip.application.result.banner.MiniappBannerResult;
import com.manzhushaka.iip.domain.IipBanner;
import com.manzhushaka.iip.service.IIipBannerService;

/**
 * 小程序轮播图 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
@RestController
@RequestMapping("/miniapp/banner")
public class MiniappBannerController extends BaseController
{
    @Autowired
    private IIipBannerService iipBannerService;

    /**
     * 查询启用中的轮播图列表。
     * 按排序升序，游客可访问；返回字段含 bannerId、title、imageUrl、linkType、linkValue、sort。
     *
     * @return 启用中的轮播图列表，无启用轮播图时返回空集合
     */
    @Anonymous
    @Log(title = "小程序查询轮播图列表", businessType = BusinessType.OTHER,
            isSaveRequestData = false, isSaveResponseData = false)
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<IipBanner> list = iipBannerService.selectEnabledIipBannerList();
        return success(list.stream().map(MiniappBannerResult::from).toList());
    }
}
