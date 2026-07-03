package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayQrcode;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PiiPayQrcodeMapper {
    int insert(PiiPayQrcode entity);
    int updateById(PiiPayQrcode entity);
    int deleteById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    PiiPayQrcode selectById(@Param("id") Long id);
    PiiPayQrcode selectByCode(@Param("qrcodeCode") String qrcodeCode);
    List<PiiPayQrcode> selectByMerchantId(@Param("merchantId") Long merchantId);
    List<PiiPayQrcode> selectExpiredBefore(@Param("time") LocalDateTime time, @Param("limit") int limit);
}
