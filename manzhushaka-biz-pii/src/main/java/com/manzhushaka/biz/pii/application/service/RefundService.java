package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.CreateRefundCommand;
import com.manzhushaka.biz.pii.application.result.RefundResult;

public interface RefundService {
    RefundResult create(CreateRefundCommand command);
}
