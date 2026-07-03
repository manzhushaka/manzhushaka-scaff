package com.manzhushaka.web.vo.pii;

import com.manzhushaka.common.annotation.Excel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PiiExportVoTest {
    @Test
    void orderAndInvoiceExportVosShouldDeclareExcelColumns() {
        assertThat(excelColumnCount(OrderVO.class)).isGreaterThanOrEqualTo(8);
        assertThat(excelColumnCount(InvoiceVO.class)).isGreaterThanOrEqualTo(8);
    }

    private long excelColumnCount(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getDeclaredAnnotations)
                .flatMap(Arrays::stream)
                .filter(annotation -> annotation.annotationType().equals(Excel.class))
                .count();
    }
}
