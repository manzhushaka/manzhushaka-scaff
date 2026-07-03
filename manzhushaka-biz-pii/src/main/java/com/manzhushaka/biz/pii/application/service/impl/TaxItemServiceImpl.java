package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.ChangeTaxItemStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateTaxItemCommand;
import com.manzhushaka.biz.pii.application.command.UpdateTaxItemCommand;
import com.manzhushaka.biz.pii.application.query.TaxItemPageQuery;
import com.manzhushaka.biz.pii.application.result.TaxItemResult;
import com.manzhushaka.biz.pii.application.service.TaxItemService;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaxItemServiceImpl implements TaxItemService {

    private final TaxItemRepository taxItemRepository;

    public TaxItemServiceImpl(TaxItemRepository taxItemRepository) {
        this.taxItemRepository = taxItemRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateTaxItemCommand command) {
        taxItemRepository.findByTaxItemCode(command.taxItemCode()).ifPresent(item -> {
            throw new ServiceException("税目编码已存在");
        });
        TaxItem taxItem = new TaxItem();
        taxItem.setTaxItemCode(command.taxItemCode());
        taxItem.setCreateTime(LocalDateTime.now());
        fillForSave(taxItem, command.name(), command.brevityCode(), command.category(), command.taxRate(),
                command.vatSpecial(), command.freeTaxType(), command.preferPolicyFlag(), command.sort(),
                command.status(), command.remark());
        return taxItemRepository.insert(taxItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateTaxItemCommand command) {
        TaxItem taxItem = taxItemRepository.findById(command.id())
                .orElseThrow(() -> new ServiceException("税目不存在"));
        taxItem.setUpdateTime(LocalDateTime.now());
        fillForSave(taxItem, command.name(), command.brevityCode(), command.category(), command.taxRate(),
                command.vatSpecial(), command.freeTaxType(), command.preferPolicyFlag(), command.sort(),
                command.status(), command.remark());
        return taxItemRepository.updateById(taxItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        taxItemRepository.findById(id).orElseThrow(() -> new ServiceException("税目不存在"));
        return taxItemRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeTaxItemStatusCommand command) {
        taxItemRepository.findById(command.id()).orElseThrow(() -> new ServiceException("税目不存在"));
        return taxItemRepository.updateStatus(command.id(), command.status());
    }

    @Override
    public TaxItemResult get(Long id) {
        return taxItemRepository.findById(id).map(TaxItemResult::from)
                .orElseThrow(() -> new ServiceException("税目不存在"));
    }

    @Override
    public List<TaxItemResult> page(TaxItemPageQuery query) {
        return taxItemRepository.findList(query.taxItemCode(), query.name(), query.status()).stream()
                .map(TaxItemResult::from)
                .collect(Collectors.toList());
    }

    private void fillForSave(TaxItem taxItem, String name, String brevityCode, String category,
                             java.math.BigDecimal taxRate, String vatSpecial, String freeTaxType,
                             String preferPolicyFlag, Integer sort, Integer status, String remark) {
        taxItem.setName(name);
        taxItem.setBrevityCode(brevityCode);
        taxItem.setCategory(category);
        taxItem.setTaxRate(taxRate);
        taxItem.setVatSpecial(vatSpecial);
        taxItem.setFreeTaxType(freeTaxType);
        taxItem.setPreferPolicyFlag(preferPolicyFlag);
        taxItem.setSort(sort == null ? 0 : sort);
        taxItem.setStatus(status == null ? 1 : status);
        taxItem.setRemark(remark);
    }
}
