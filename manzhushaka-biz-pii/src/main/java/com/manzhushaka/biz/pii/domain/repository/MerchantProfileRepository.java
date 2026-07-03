package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;

import java.util.List;
import java.util.Optional;

public interface MerchantProfileRepository {
    Long insert(MerchantProfile profile);
    int updateById(MerchantProfile profile);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Optional<MerchantProfile> findById(Long id);
    Optional<MerchantProfile> findByDeptId(Long deptId);
    Optional<MerchantProfile> findByUmsMerchantAndTerminal(String umsMerchantId, String umsTerminalId);
    List<MerchantProfile> findByStatus(Integer status);
    List<MerchantProfile> findList(String merchantName, String umsMerchantId, Integer status);
}
