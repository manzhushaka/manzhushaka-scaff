package com.manzhushaka.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqMessageStatusRuleTest {

    @Test
    void initAllowsManualRetry() {
        assertTrue(MqMessageStatus.INIT.allowsManualRetry(false));
    }

    @Test
    void failAllowsManualRetry() {
        assertTrue(MqMessageStatus.FAIL.allowsManualRetry(false));
    }

    @Test
    void publishedDoesNotAllowManualRetry() {
        assertFalse(MqMessageStatus.PUBLISHED.allowsManualRetry(false));
    }

    @Test
    void processingOnlyAllowsManualRetryAfterTimeout() {
        assertFalse(MqMessageStatus.PROCESSING.allowsManualRetry(false));
        assertTrue(MqMessageStatus.PROCESSING.allowsManualRetry(true));
    }

    @Test
    void successDoesNotAllowManualRetry() {
        assertFalse(MqMessageStatus.SUCCESS.allowsManualRetry(false));
        assertFalse(MqMessageStatus.SUCCESS.allowsManualRetry(true));
    }
}
