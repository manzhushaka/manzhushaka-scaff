package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcodeTaxItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PiiPayQrcodeTaxItemMapper {
    int insert(PiiPayQrcodeTaxItem entity);
    int updateById(PiiPayQrcodeTaxItem entity);
    int deleteById(@Param("id") Long id);
    int deleteByQrcodeId(@Param("qrcodeId") Long qrcodeId);
    int deleteByQrcodeIdAndTaxItemId(@Param("qrcodeId") Long qrcodeId, @Param("taxItemId") Long taxItemId);
    PiiPayQrcodeTaxItem selectByQrcodeIdAndTaxItemId(@Param("qrcodeId") Long qrcodeId, @Param("taxItemId") Long taxItemId);
    List<PiiPayQrcodeTaxItem> selectByQrcodeId(@Param("qrcodeId") Long qrcodeId);
}
