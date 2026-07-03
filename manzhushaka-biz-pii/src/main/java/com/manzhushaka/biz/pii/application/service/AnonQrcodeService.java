package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.result.QrcodeConfigResult;

public interface AnonQrcodeService {
    QrcodeConfigResult getConfig(String code);
}
