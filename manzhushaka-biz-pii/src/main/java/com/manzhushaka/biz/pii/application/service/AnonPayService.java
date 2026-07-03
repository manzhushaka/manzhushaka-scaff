package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.PrecreatePayCommand;
import com.manzhushaka.biz.pii.application.result.PrecreatePayResult;

public interface AnonPayService {
    PrecreatePayResult precreate(PrecreatePayCommand command);
}
