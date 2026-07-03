package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.TaxItem;

import java.util.List;
import java.util.Optional;

public interface TaxItemRepository {
    Long insert(TaxItem taxItem);
    int updateById(TaxItem taxItem);
    int updateStatus(Long id, Integer status);
    Optional<TaxItem> findById(Long id);
    Optional<TaxItem> findByTaxItemCode(String taxItemCode);
    List<TaxItem> findEnabled();
    List<TaxItem> findByNameLike(String name);
}
