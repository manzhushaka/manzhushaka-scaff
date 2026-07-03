package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.InvoicePageQuery;
import com.manzhushaka.biz.pii.application.result.InvoiceResult;
import com.manzhushaka.biz.pii.application.service.impl.InvoiceQueryServiceImpl;
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

class InvoiceQueryServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final InvoiceQueryService service = new InvoiceQueryServiceImpl(payOrderRepository);

    @Test
    void pageShouldDelegateInvoiceFiltersAndConvertResult() {
        LocalDateTime begin = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        when(payOrderRepository.findInvoiceList(100L, "ORDER001", "INV001", "ISSUED", begin, end))
                .thenReturn(List.of(order()));

        List<InvoiceResult> results = service.page(new InvoicePageQuery(100L, "ORDER001", "INV001", "ISSUED", begin, end));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOutTradeNo()).isEqualTo("ORDER001");
        assertThat(results.get(0).getInvoiceNo()).isEqualTo("INV001");
        assertThat(results.get(0).getInvoiceStatus()).isEqualTo("ISSUED");
        verify(payOrderRepository).findInvoiceList(100L, "ORDER001", "INV001", "ISSUED", begin, end);
    }

    @Test
    void getShouldThrowWhenOrderMissing() {
        when(payOrderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(10L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("发票记录不存在");
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setOutTradeNo("ORDER001");
        order.setAmount(8800L);
        order.setBuyerName("测试公司");
        order.setBuyerTaxCode("91460000MA001");
        order.setBuyerEmail("buyer@example.com");
        order.setInvoiceStatus("ISSUED");
        order.setInvoiceNo("INV001");
        order.setInvoiceCode("CODE001");
        order.setInvoicePdfUrl("https://example.com/invoice.pdf");
        order.setInvoiceIssueTime(LocalDateTime.of(2026, 7, 2, 10, 0));
        return order;
    }
}
