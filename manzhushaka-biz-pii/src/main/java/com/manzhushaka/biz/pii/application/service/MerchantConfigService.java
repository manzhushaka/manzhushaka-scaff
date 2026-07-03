package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.UpdateMerchantConfigCommand;
import com.manzhushaka.biz.pii.application.result.MerchantConfigResult;

public interface MerchantConfigService {
    MerchantConfigResult getByDeptId(Long deptId);
    int update(UpdateMerchantConfigCommand command);
}
