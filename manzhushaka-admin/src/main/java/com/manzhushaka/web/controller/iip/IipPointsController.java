package com.manzhushaka.web.controller.iip;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.iip.application.points.result.PointsAccountResult;
import com.manzhushaka.iip.application.points.result.PointsRecordResult;
import com.manzhushaka.iip.application.points.service.PointsAdminAppService;
import com.manzhushaka.web.converter.iip.PointsAdminConverter;
import com.manzhushaka.web.dto.iip.PointsAccountQueryRequest;
import com.manzhushaka.web.dto.iip.PointsAdjustRequest;
import com.manzhushaka.web.dto.iip.PointsRecordQueryRequest;

/**
 * 积分管理 信息操作处理
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/points")
public class IipPointsController extends BaseController
{
    @Autowired
    private PointsAdminAppService pointsAdminAppService;

    /**
     * 获取积分账户列表
     */
    @PreAuthorize("@ss.hasPermi('iip:points:list')")
    @GetMapping("/account/list")
    public TableDataInfo accountList(PointsAccountQueryRequest request)
    {
        startPage();
        List<PointsAccountResult> list = pointsAdminAppService
                .listPointsAccounts(PointsAdminConverter.toAccountQuery(request));
        return getDataTable(list);
    }

    /**
     * 获取积分流水列表
     */
    @PreAuthorize("@ss.hasPermi('iip:points:query')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(PointsRecordQueryRequest request)
    {
        startPage();
        List<PointsRecordResult> list = pointsAdminAppService
                .listPointsRecords(PointsAdminConverter.toRecordQuery(request));
        return getDataTable(list);
    }

    /**
     * 手工调整积分（正数发放、负数扣减）
     */
    @PreAuthorize("@ss.hasPermi('iip:points:adjust')")
    @Log(title = "积分流水", businessType = BusinessType.UPDATE)
    @PostMapping("/adjust")
    public AjaxResult adjust(@Validated @RequestBody PointsAdjustRequest request)
    {
        pointsAdminAppService.adjustPoints(PointsAdminConverter.toCommand(request));
        return success();
    }
}
