package com.manzhushaka.system.controller;

import com.manzhushaka.framework.exception.GlobalExceptionHandler;
import com.manzhushaka.mq.service.MqMessageAdminService;
import com.manzhushaka.system.service.LogQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogQueryControllerMqMessageEndpointTest {

    @Test
    void retryMqMessageShouldDelegateToAdminService() throws Exception {
        LogQueryService logQueryService = mock(LogQueryService.class);
        MqMessageAdminService mqMessageAdminService = mock(MqMessageAdminService.class);
        LogQueryController controller = new LogQueryController(logQueryService, mqMessageAdminService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        mockMvc.perform(post("/system/logs/mq-messages/100/retry"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(mqMessageAdminService).retry(100L);
    }
}
