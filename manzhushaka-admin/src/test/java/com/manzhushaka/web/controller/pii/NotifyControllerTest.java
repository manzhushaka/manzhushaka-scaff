package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.service.InvoiceNotifyService;
import com.manzhushaka.biz.pii.application.service.PayNotifyService;
import com.manzhushaka.biz.pii.application.service.RefundNotifyService;
import com.manzhushaka.biz.pii.application.service.RefundService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotifyControllerTest {

    private final PayNotifyService payNotifyService = mock(PayNotifyService.class);
    private final InvoiceNotifyService invoiceNotifyService = mock(InvoiceNotifyService.class);
    private final RefundNotifyService refundNotifyService = mock(RefundNotifyService.class);
    private final RefundService refundService = mock(RefundService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new PayNotifyController(payNotifyService),
            new InvoiceNotifyController(invoiceNotifyService),
            new RefundController(refundService, refundNotifyService)
    ).build();

    @Test
    void payNotifyShouldReturnFailWhenSignatureInvalid() throws Exception {
        String body = "{\"outTradeNo\":\"ORDER001\",\"tradeStatus\":\"SUCCESS\"}";
        when(payNotifyService.notify(body, "BAD_SIGN")).thenReturn("FAIL");

        mockMvc.perform(post("/pii/pay/notify")
                        .queryParam("sign", "BAD_SIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("FAIL"));

        verify(payNotifyService).notify(body, "BAD_SIGN");
    }

    @Test
    void payNotifyShouldReturnSuccessForDuplicateAndNormalCallbacks() throws Exception {
        String duplicateBody = "{\"outTradeNo\":\"ORDER001\",\"tradeStatus\":\"SUCCESS\",\"duplicate\":true}";
        String normalBody = "{\"outTradeNo\":\"ORDER002\",\"tradeStatus\":\"SUCCESS\",\"tradeNo\":\"TRADE002\"}";
        when(payNotifyService.notify(duplicateBody, "SIGN1")).thenReturn("SUCCESS");
        when(payNotifyService.notify(normalBody, "SIGN2")).thenReturn("SUCCESS");

        mockMvc.perform(post("/pii/pay/notify")
                        .queryParam("sign", "SIGN1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));
        mockMvc.perform(post("/pii/pay/notify")
                        .queryParam("sign", "SIGN2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(normalBody))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        verify(payNotifyService).notify(duplicateBody, "SIGN1");
        verify(payNotifyService).notify(normalBody, "SIGN2");
    }

    @Test
    void invoiceNotifyShouldPassIssuedReversedAndFailedCallbacksToService() throws Exception {
        assertInvoiceNotifyStatus("ISSUED");
        assertInvoiceNotifyStatus("REVERSED");
        assertInvoiceNotifyStatus("FAILED");
    }

    @Test
    void refundNotifyShouldReturnSuccessAndPassRawBodyToService() throws Exception {
        String body = "{\"refundOrderId\":\"REFUND001\",\"tradeStatus\":\"SUCCESS\"}";
        when(refundNotifyService.notify(body, "SIGN")).thenReturn("SUCCESS");

        mockMvc.perform(post("/pii/refund/notify")
                        .queryParam("sign", "SIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        verify(refundNotifyService).notify(body, "SIGN");
    }

    private void assertInvoiceNotifyStatus(String invoiceStatus) throws Exception {
        String body = "{\"merOrderId\":\"ORDER001\",\"merOrderDate\":\"20260703\",\"status\":\"" + invoiceStatus + "\"}";
        String sign = "SIGN_" + invoiceStatus;
        when(invoiceNotifyService.notify(body, sign)).thenReturn("SUCCESS");

        mockMvc.perform(post("/pii/invoice/notify")
                        .queryParam("sign", sign)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        verify(invoiceNotifyService).notify(body, sign);
    }
}
