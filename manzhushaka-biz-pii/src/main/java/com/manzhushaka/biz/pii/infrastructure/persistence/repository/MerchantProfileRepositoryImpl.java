package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.MerchantProfileConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiMerchantProfile;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiMerchantProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MerchantProfileRepositoryImpl implements MerchantProfileRepository {
    private final PiiMerchantProfileMapper mapper;

    public MerchantProfileRepositoryImpl(PiiMerchantProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long insert(MerchantProfile profile) {
        PiiMerchantProfile entity = MerchantProfileConverter.toEntity(profile);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public int updateById(MerchantProfile profile) {
        return mapper.updateById(MerchantProfileConverter.toEntity(profile));
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return mapper.updateStatus(id, status);
    }

    @Override
    public Optional<MerchantProfile> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(MerchantProfileConverter::toDomain);
    }

    @Override
    public Optional<MerchantProfile> findByDeptId(Long deptId) {
        return Optional.ofNullable(mapper.selectByDeptId(deptId)).map(MerchantProfileConverter::toDomain);
    }

    @Override
    public Optional<MerchantProfile> findByUmsMerchantAndTerminal(String umsMerchantId, String umsTerminalId) {
        return Optional.ofNullable(mapper.selectByUmsMerchantAndTerminal(umsMerchantId, umsTerminalId)).map(MerchantProfileConverter::toDomain);
    }

    @Override
    public List<MerchantProfile> findByStatus(Integer status) {
        return mapper.selectByStatus(status).stream().map(MerchantProfileConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MerchantProfile> findList(String merchantName, String umsMerchantId, Integer status) {
        return mapper.selectList(merchantName, umsMerchantId, status).stream().map(MerchantProfileConverter::toDomain).collect(Collectors.toList());
    }
}
