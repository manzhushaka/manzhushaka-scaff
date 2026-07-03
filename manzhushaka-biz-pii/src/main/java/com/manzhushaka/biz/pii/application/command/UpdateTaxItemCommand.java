package com.manzhushaka.biz.pii.application.command;

import java.math.BigDecimal;

public record UpdateTaxItemCommand(
        Long id,
        String name,
        String brevityCode,
        String category,
        BigDecimal taxRate,
        String vatSpecial,
        String freeTaxType,
        String preferPolicyFlag,
        Integer sort,
        Integer status,
        String remark
) {
}
