package com.manzhushaka.biz.pii.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PiiDomainSkeletonTest {
    private static final String MODEL_PACKAGE = "com.manzhushaka.biz.pii.domain.model.";
    private static final String REPOSITORY_PACKAGE = "com.manzhushaka.biz.pii.domain.repository.";

    @Test
    void hasNineDomainModelsAndRepositories() throws Exception {
        List<String> names = Arrays.asList(
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

        for (String name : names) {
            assertNotNull(Class.forName(MODEL_PACKAGE + name));
            assertTrue(Class.forName(REPOSITORY_PACKAGE + name + "Repository").isInterface());
        }
    }

    @Test
    void keyFieldsFollowSpecTypes() throws Exception {
        assertFieldType("TaxItem", "taxRate", BigDecimal.class);
        assertFieldType("PayOrder", "payTime", LocalDateTime.class);
        assertFieldType("PayOrder", "invoiceIssueTime", LocalDateTime.class);
        assertFieldType("RefundRecord", "completeTime", LocalDateTime.class);
        assertFieldType("InvoiceCallLog", "durationMs", Integer.class);
        assertFieldType("PaymentNotifyLog", "notifyPayload", String.class);
        assertFieldType("InvoiceNotifyLog", "notifyPayload", String.class);
    }

    @Test
    void toStringAvoidsSensitiveAndLongFields() throws Exception {
        Class<?> orderClass = Class.forName(MODEL_PACKAGE + "PayOrder");
        Object order = orderClass.getDeclaredConstructor().newInstance();
        orderClass.getMethod("setOutTradeNo", String.class).invoke(order, "26070212345678900000000000000001");
        orderClass.getMethod("setAmount", Long.class).invoke(order, 100L);
        orderClass.getMethod("setPayStatus", String.class).invoke(order, "PAID");
        orderClass.getMethod("setInvoiceStatus", String.class).invoke(order, "ISSUED");
        orderClass.getMethod("setBuyerEmail", String.class).invoke(order, "buyer@example.com");
        orderClass.getMethod("setBuyerMobile", String.class).invoke(order, "13800000000");
        orderClass.getMethod("setOrderToken", String.class).invoke(order, "secret-token");
        orderClass.getMethod("setClientIp", String.class).invoke(order, "127.0.0.1");

        String orderText = order.toString();
        assertTrue(orderText.contains("26070212345678900000000000000001"));
        assertFalse(orderText.contains("buyer@example.com"));
        assertFalse(orderText.contains("13800000000"));
        assertFalse(orderText.contains("secret-token"));
        assertFalse(orderText.contains("127.0.0.1"));

        Class<?> notifyClass = Class.forName(MODEL_PACKAGE + "PaymentNotifyLog");
        Object notifyLog = notifyClass.getDeclaredConstructor().newInstance();
        notifyClass.getMethod("setOutTradeNo", String.class).invoke(notifyLog, "26070212345678900000000000000001");
        notifyClass.getMethod("setNotifyPayload", String.class).invoke(notifyLog, "{\"very\":\"long-payload\"}");
        notifyClass.getMethod("setSign", String.class).invoke(notifyLog, "secret-sign");

        String notifyText = notifyLog.toString();
        assertTrue(notifyText.contains("26070212345678900000000000000001"));
        assertFalse(notifyText.contains("long-payload"));
        assertFalse(notifyText.contains("secret-sign"));
    }

    private void assertFieldType(String className, String fieldName, Class<?> expectedType) throws Exception {
        Field field = Class.forName(MODEL_PACKAGE + className).getDeclaredField(fieldName);
        assertEquals(expectedType, field.getType());
    }
}
