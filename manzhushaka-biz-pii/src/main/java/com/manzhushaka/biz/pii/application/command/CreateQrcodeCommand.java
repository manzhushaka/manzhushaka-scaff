package com.manzhushaka.biz.pii.application.command;

import java.time.LocalDateTime;
import java.util.List;

public record CreateQrcodeCommand(
        Long merchantId,
        String qrcodeCode,
        String name,
        Integer status,
        LocalDateTime expireTime,
        String remark,
        List<QrcodeTaxItemCommand> taxItems
) {
}
