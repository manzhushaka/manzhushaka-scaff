package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.query.InvoicePageQuery;
import com.manzhushaka.biz.pii.application.result.InvoiceResult;
import com.manzhushaka.biz.pii.application.service.InvoiceQueryService;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceQueryServiceImpl implements InvoiceQueryService {
    private final PayOrderRepository payOrderRepository;

    public InvoiceQueryServiceImpl(PayOrderRepository payOrderRepository) {
        this.payOrderRepository = payOrderRepository;
    }

    @Override
    public List<InvoiceResult> page(InvoicePageQuery query) {
        return payOrderRepository.findInvoiceList(query.merchantId(), query.outTradeNo(), query.invoiceNo(),
                        query.invoiceStatus(), query.invoiceIssueTimeBegin(), query.invoiceIssueTimeEnd())
                .stream().map(InvoiceResult::from).collect(Collectors.toList());
    }

    @Override
    public InvoiceResult get(Long orderId) {
        return payOrderRepository.findById(orderId).map(InvoiceResult::from)
                .orElseThrow(() -> new ServiceException("发票记录不存在"));
    }
}
