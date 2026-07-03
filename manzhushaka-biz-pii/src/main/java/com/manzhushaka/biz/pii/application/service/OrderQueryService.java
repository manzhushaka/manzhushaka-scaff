package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.OrderPageQuery;
import com.manzhushaka.biz.pii.application.result.OrderResult;

import java.util.List;

public interface OrderQueryService {
    List<OrderResult> page(OrderPageQuery query);
    OrderResult get(Long id);
}
