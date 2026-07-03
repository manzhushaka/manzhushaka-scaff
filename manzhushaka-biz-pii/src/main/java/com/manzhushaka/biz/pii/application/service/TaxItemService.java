package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.ChangeTaxItemStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateTaxItemCommand;
import com.manzhushaka.biz.pii.application.command.UpdateTaxItemCommand;
import com.manzhushaka.biz.pii.application.query.TaxItemPageQuery;
import com.manzhushaka.biz.pii.application.result.TaxItemResult;

import java.util.List;

public interface TaxItemService {
    Long create(CreateTaxItemCommand command);
    int update(UpdateTaxItemCommand command);
    int delete(Long id);
    int changeStatus(ChangeTaxItemStatusCommand command);
    TaxItemResult get(Long id);
    List<TaxItemResult> page(TaxItemPageQuery query);
}
