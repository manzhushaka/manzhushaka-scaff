package com.manzhushaka.web.controller.iip;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.iip.application.exchange.result.ExchangeRecordResult;
import com.manzhushaka.iip.application.exchange.service.ExchangeAppService;
import com.manzhushaka.web.converter.iip.ExchangeAdminConverter;
import com.manzhushaka.web.dto.iip.ExchangeQueryRequest;
import com.manzhushaka.web.dto.iip.ExchangeVoidRequest;
import com.manzhushaka.framework.security.context.SecurityContextHelper;

/**
 * 兑换记录 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/exchange")
public class IipExchangeController extends BaseController
{
    @Autowired
    private ExchangeAppService exchangeAppService;

    /**
     * 获取兑换记录列表（按券名/用户/状态/核销码/兑换时间筛选）
     */
    @PreAuthorize("@ss.hasPermi('iip:exchange:list')")
    @GetMapping("/list")
    public TableDataInfo list(ExchangeQueryRequest request)
    {
        startPage();
        List<ExchangeRecordResult> list = exchangeAppService
                .listExchangeRecords(ExchangeAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 导出兑换记录列表
     */
    @Log(title = "兑换记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('iip:exchange:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, ExchangeQueryRequest request)
    {
        List<ExchangeRecordResult> list = exchangeAppService
                .listExchangeRecords(ExchangeAdminConverter.toQuery(request));
        ExcelUtil<ExchangeRecordResult> util = new ExcelUtil<ExchangeRecordResult>(ExchangeRecordResult.class);
        util.exportExcel(response, list, "兑换记录数据");
    }

    /**
     * 根据记录ID获取兑换记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:exchange:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(exchangeAppService.getExchangeRecord(recordId));
    }

    /**
     * 作废未使用券，恢复库存和活动额度并退回兑换积分。
     */
    @PreAuthorize("@ss.hasPermi('iip:exchange:void')")
    @Log(title = "兑换记录", businessType = BusinessType.UPDATE)
    @PutMapping("/{recordId}/void")
    public AjaxResult voidExchange(@PathVariable Long recordId,
            @Validated @RequestBody ExchangeVoidRequest request)
    {
        exchangeAppService.voidExchange(recordId, SecurityContextHelper.getUsername(), request.getVoidReason());
        return success();
    }
}
