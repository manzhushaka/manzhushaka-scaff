package com.manzhushaka.biz.pii.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PiiPersistenceSkeletonTest {
    private static final String ENTITY_PACKAGE = "com.manzhushaka.biz.pii.infrastructure.persistence.entity.Pii";
    private static final String MAPPER_PACKAGE = "com.manzhushaka.biz.pii.infrastructure.persistence.mapper.Pii";
    private static final Path XML_DIR = Path.of("src/main/resources/mapper/pii");

    @Test
    void hasNineEntitiesMappersAndXmlFiles() throws Exception {
        for (String name : names()) {
            assertNotNull(Class.forName(ENTITY_PACKAGE + name));
            assertTrue(Class.forName(MAPPER_PACKAGE + name + "Mapper").isInterface());
            assertTrue(Files.exists(XML_DIR.resolve("Pii" + name + "Mapper.xml")));
        }
    }

    @Test
    void payOrderMapperExposesBusinessQueries() throws Exception {
        Class<?> mapper = Class.forName(MAPPER_PACKAGE + "PayOrderMapper");
        method(mapper, "insert");
        method(mapper, "updateById");
        method(mapper, "updatePayStatus");
        method(mapper, "updateInvoiceStatus");
        method(mapper, "updateRefundAmountAndStatus");
        method(mapper, "selectById");
        method(mapper, "selectByOutTradeNo");
        method(mapper, "selectByOutTradeNoAndToken");
        method(mapper, "selectPendingBefore");
    }

    @Test
    void payOrderXmlMapsDelFlagColumn() throws Exception {
        String xml = Files.readString(XML_DIR.resolve("PiiPayOrderMapper.xml"));
        assertTrue(xml.contains("property=\"delFlag\""));
        assertTrue(xml.contains("column=\"del_flag\""));
        assertTrue(xml.contains("where id = #{id} and del_flag = 0"));
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
