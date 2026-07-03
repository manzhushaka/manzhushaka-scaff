package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.TaxItemConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiTaxItem;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiTaxItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TaxItemRepositoryImpl implements TaxItemRepository {
    private final PiiTaxItemMapper mapper;

    public TaxItemRepositoryImpl(PiiTaxItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(TaxItem taxItem) {
        PiiTaxItem entity = TaxItemConverter.toEntity(taxItem);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateById(TaxItem taxItem) {
        return mapper.updateById(TaxItemConverter.toEntity(taxItem));
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return mapper.updateStatus(id, status);
    }

    @Override
    public Optional<TaxItem> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(TaxItemConverter::toDomain);
    }

    @Override
    public Optional<TaxItem> findByTaxItemCode(String taxItemCode) {
        return Optional.ofNullable(mapper.selectByTaxItemCode(taxItemCode)).map(TaxItemConverter::toDomain);
    }

    @Override
    public List<TaxItem> findEnabled() {
        return mapper.selectEnabled().stream().map(TaxItemConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<TaxItem> findByNameLike(String name) {
        return mapper.selectByNameLike(name).stream().map(TaxItemConverter::toDomain).collect(Collectors.toList());
    }
}
