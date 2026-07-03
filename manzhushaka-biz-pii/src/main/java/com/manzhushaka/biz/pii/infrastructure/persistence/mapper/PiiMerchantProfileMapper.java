package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiMerchantProfile;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiMerchantProfileMapper {
    int insert(PiiMerchantProfile entity);
    int updateById(PiiMerchantProfile entity);
    int deleteById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    PiiMerchantProfile selectById(@Param("id") Long id);
    PiiMerchantProfile selectByDeptId(@Param("deptId") Long deptId);
    PiiMerchantProfile selectByUmsMerchantAndTerminal(@Param("umsMerchantId") String umsMerchantId, @Param("umsTerminalId") String umsTerminalId);
    List<PiiMerchantProfile> selectByStatus(@Param("status") Integer status);
    List<PiiMerchantProfile> selectList(@Param("merchantName") String merchantName, @Param("umsMerchantId") String umsMerchantId, @Param("status") Integer status);
}
