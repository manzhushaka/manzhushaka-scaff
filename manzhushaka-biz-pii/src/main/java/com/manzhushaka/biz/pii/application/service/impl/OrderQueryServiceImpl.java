package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.query.OrderPageQuery;
import com.manzhushaka.biz.pii.application.result.OrderResult;
import com.manzhushaka.biz.pii.application.service.OrderQueryService;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    private final PayOrderRepository payOrderRepository;

    public OrderQueryServiceImpl(PayOrderRepository payOrderRepository) {
        this.payOrderRepository = payOrderRepository;
    }

    @Override
    public List<OrderResult> page(OrderPageQuery query) {
        return payOrderRepository.findList(query.merchantId(), query.outTradeNo(), query.payStatus(),
                        query.invoiceStatus(), query.payTimeBegin(), query.payTimeEnd())
                .stream().map(OrderResult::from).collect(Collectors.toList());
    }

    @Override
    public OrderResult get(Long id) {
        return payOrderRepository.findById(id).map(OrderResult::from)
                .orElseThrow(() -> new ServiceException("订单不存在"));
    }
}
