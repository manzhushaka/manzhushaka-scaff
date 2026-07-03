package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.ChangeMerchantStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateMerchantCommand;
import com.manzhushaka.biz.pii.application.command.UpdateMerchantCommand;
import com.manzhushaka.biz.pii.application.query.MerchantPageQuery;
import com.manzhushaka.biz.pii.application.result.MerchantResult;

import java.util.List;

public interface MerchantService {
    Long create(CreateMerchantCommand command);
    int update(UpdateMerchantCommand command);
    int delete(Long id);
    int changeStatus(ChangeMerchantStatusCommand command);
    MerchantResult get(Long id);
    List<MerchantResult> page(MerchantPageQuery query);
}
