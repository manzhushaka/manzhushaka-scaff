package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiTaxItem;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiTaxItemMapper {
    int insert(PiiTaxItem entity);
    int updateById(PiiTaxItem entity);
    int deleteById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    PiiTaxItem selectById(@Param("id") Long id);
    PiiTaxItem selectByTaxItemCode(@Param("taxItemCode") String taxItemCode);
    List<PiiTaxItem> selectEnabled();
    List<PiiTaxItem> selectByNameLike(@Param("name") String name);
    List<PiiTaxItem> selectList(@Param("taxItemCode") String taxItemCode, @Param("name") String name, @Param("status") Integer status);
}
