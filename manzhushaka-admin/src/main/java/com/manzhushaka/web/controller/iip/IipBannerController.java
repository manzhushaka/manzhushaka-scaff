package com.manzhushaka.web.controller.iip;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.domain.IipBanner;
import com.manzhushaka.iip.service.IIipBannerService;

/**
 * 轮播图管理 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
@RestController
@RequestMapping("/iip/banner")
public class IipBannerController extends BaseController
{
    @Autowired
    private IIipBannerService iipBannerService;

    /**
     * 获取轮播图列表（按标题/状态筛选）
     */
    @PreAuthorize("@ss.hasPermi('iip:banner:list')")
    @GetMapping("/list")
    public TableDataInfo list(IipBanner iipBanner)
    {
        startPage();
        List<IipBanner> list = iipBannerService.selectIipBannerList(iipBanner);
        return getDataTable(list);
    }

    /**
     * 根据轮播图ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:banner:query')")
    @GetMapping(value = "/{bannerId}")
    public AjaxResult getInfo(@PathVariable("bannerId") Long bannerId)
    {
        return success(iipBannerService.selectIipBannerById(bannerId));
    }

    /**
     * 新增轮播图
     */
    @PreAuthorize("@ss.hasPermi('iip:banner:add')")
    @Log(title = "轮播图管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody IipBanner iipBanner)
    {
        iipBanner.setCreateBy(SecurityContextHelper.getUsername());
        return toAjax(iipBannerService.insertIipBanner(iipBanner));
    }

    /**
     * 修改轮播图
     */
    @PreAuthorize("@ss.hasPermi('iip:banner:edit')")
    @Log(title = "轮播图管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody IipBanner iipBanner)
    {
        iipBanner.setUpdateBy(SecurityContextHelper.getUsername());
        return toAjax(iipBannerService.updateIipBanner(iipBanner));
    }

    /**
     * 删除轮播图
     */
    @PreAuthorize("@ss.hasPermi('iip:banner:remove')")
    @Log(title = "轮播图管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bannerIds}")
    public AjaxResult remove(@PathVariable("bannerIds") Long[] bannerIds)
    {
        return toAjax(iipBannerService.deleteIipBannerByIds(bannerIds));
    }
}
