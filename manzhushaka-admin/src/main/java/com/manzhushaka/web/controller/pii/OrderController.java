package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.OrderPageQuery;
import com.manzhushaka.biz.pii.application.service.OrderQueryService;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.web.dto.pii.OrderPageRequest;
import com.manzhushaka.web.vo.pii.OrderVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        List<OrderVO> rows = orderQueryService.page(new OrderPageQuery(
                        request.getMerchantId(),
                        request.getOutTradeNo(),
                        request.getPayStatus(),
                        request.getInvoiceStatus(),
                        request.getPayTimeBegin(),
                        request.getPayTimeEnd()))
                .stream().map(OrderVO::from).collect(Collectors.toList());
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('biz:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(OrderVO.from(orderQueryService.get(id)));
    }
}
