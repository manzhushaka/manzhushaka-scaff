package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.OrderPageQuery;
import com.manzhushaka.biz.pii.application.service.OrderQueryService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.web.dto.pii.OrderPageRequest;
import com.manzhushaka.web.vo.pii.OrderVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pii/order")
public class OrderController extends BaseController {
    private final OrderQueryService orderQueryService;

    public OrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @PreAuthorize("@ss.hasPermi('biz:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderPageRequest request) {
        startPage();
        List<OrderVO> rows = listRows(request);
        return getDataTable(rows);
    }

    @Log(title = "订单查询", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('biz:order:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderPageRequest request) {
        ExcelUtil<OrderVO> util = new ExcelUtil<>(OrderVO.class);
        util.exportExcel(response, listRows(request), "订单数据");
    }

    @PreAuthorize("@ss.hasPermi('biz:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(OrderVO.from(orderQueryService.get(id)));
    }

    private List<OrderVO> listRows(OrderPageRequest request) {
        return orderQueryService.page(new OrderPageQuery(
                        request.getMerchantId(),
                        request.getOutTradeNo(),
                        request.getPayStatus(),
                        request.getInvoiceStatus(),
                        request.getPayTimeBegin(),
                        request.getPayTimeEnd()))
                .stream().map(OrderVO::from).collect(Collectors.toList());
    }
}
