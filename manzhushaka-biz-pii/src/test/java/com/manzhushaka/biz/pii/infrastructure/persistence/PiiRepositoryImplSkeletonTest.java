package com.manzhushaka.biz.pii.infrastructure.persistence;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PiiRepositoryImplSkeletonTest {
    private static final String DOMAIN_REPOSITORY_PACKAGE = "com.manzhushaka.biz.pii.domain.repository.";
    private static final String CONVERTER_PACKAGE = "com.manzhushaka.biz.pii.infrastructure.persistence.converter.";
    private static final String REPOSITORY_IMPL_PACKAGE = "com.manzhushaka.biz.pii.infrastructure.persistence.repository.";

    @Test
    void hasNineConvertersAndRepositoryImplementations() throws Exception {
        for (String name : names()) {
            Class<?> converter = Class.forName(CONVERTER_PACKAGE + name + "Converter");
            assertNotNull(method(converter, "toDomain"));
            assertNotNull(method(converter, "toEntity"));

            Class<?> repository = Class.forName(DOMAIN_REPOSITORY_PACKAGE + name + "Repository");
            Class<?> implementation = Class.forName(REPOSITORY_IMPL_PACKAGE + name + "RepositoryImpl");
            assertTrue(repository.isAssignableFrom(implementation));
        }
    }

    @Test
    void payOrderConverterMapsCoreFields() throws Exception {
        PayOrder domain = new PayOrder();
        domain.setId(100L);
        domain.setMerchantId(200L);
        domain.setOutTradeNo("26070212345678900000000000000001");
        domain.setAmount(300L);
        domain.setPayStatus("PAID");
        domain.setInvoiceStatus("ISSUED");

        Class<?> converter = Class.forName(CONVERTER_PACKAGE + "PayOrderConverter");
        PiiPayOrder entity = (PiiPayOrder) converter.getMethod("toEntity", PayOrder.class).invoke(null, domain);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getMerchantId(), entity.getMerchantId());
        assertEquals(domain.getOutTradeNo(), entity.getOutTradeNo());
        assertEquals(domain.getAmount(), entity.getAmount());
        assertEquals(domain.getPayStatus(), entity.getPayStatus());
        assertEquals(domain.getInvoiceStatus(), entity.getInvoiceStatus());

        PayOrder mapped = (PayOrder) converter.getMethod("toDomain", PiiPayOrder.class).invoke(null, entity);
        assertEquals(domain.getId(), mapped.getId());
        assertEquals(domain.getMerchantId(), mapped.getMerchantId());
        assertEquals(domain.getOutTradeNo(), mapped.getOutTradeNo());
        assertEquals(domain.getAmount(), mapped.getAmount());
        assertEquals(domain.getPayStatus(), mapped.getPayStatus());
        assertEquals(domain.getInvoiceStatus(), mapped.getInvoiceStatus());
    }

    private List<String> names() {
        return Arrays.asList(
                "TaxItem",
                "MerchantProfile",
                "PayQrcode",
                "PayQrcodeTaxItem",
                "PayOrder",
                "RefundRecord",
                "PaymentNotifyLog",
                "InvoiceNotifyLog",
                "InvoiceCallLog"
        );
    }

    private Method method(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> name.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing method " + name + " on " + type.getName()));
    }
}
