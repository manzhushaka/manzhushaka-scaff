package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.OrderPageQuery;
import com.manzhushaka.biz.pii.application.result.OrderResult;
import com.manzhushaka.biz.pii.application.service.impl.OrderQueryServiceImpl;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderQueryServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final OrderQueryService service = new OrderQueryServiceImpl(payOrderRepository);

    @Test
    void pageShouldDelegateFiltersAndConvertOrders() {
        LocalDateTime begin = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        PayOrder order = order();
        when(payOrderRepository.findList(100L, "ORDER001", "PAID", "ISSUED", begin, end))
                .thenReturn(List.of(order));

        List<OrderResult> results = service.page(new OrderPageQuery(100L, "ORDER001", "PAID", "ISSUED", begin, end));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOutTradeNo()).isEqualTo("ORDER001");
        assertThat(results.get(0).getAmount()).isEqualTo(8800L);
        assertThat(results.get(0).getInvoiceStatus()).isEqualTo("ISSUED");
        verify(payOrderRepository).findList(100L, "ORDER001", "PAID", "ISSUED", begin, end);
    }

    @Test
    void getShouldThrowWhenOrderMissing() {
        when(payOrderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(10L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单不存在");
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setQrcodeId(200L);
        order.setTaxItemId(300L);
        order.setOutTradeNo("ORDER001");
        order.setAmount(8800L);
        order.setBuyerName("测试公司");
        order.setPayStatus("PAID");
        order.setPayTradeNo("TRADE001");
        order.setRefundAmount(0L);
        order.setInvoiceStatus("ISSUED");
        order.setInvoiceNo("INV001");
        order.setInvoiceCode("CODE001");
        return order;
    }
}
