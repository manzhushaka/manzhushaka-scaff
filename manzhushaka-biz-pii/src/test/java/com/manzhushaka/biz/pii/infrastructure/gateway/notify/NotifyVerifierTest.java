package com.manzhushaka.biz.pii.infrastructure.gateway.notify;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyVerifierTest {

    @Test
    void signShouldIgnoreEmptyValuesAndSignThenSortByKey() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        params.put("notifyUrl", "");
        params.put("remark", null);
        params.put("sign", "SHOULD_BE_IGNORED");

        String sign = NotifyVerifier.sign(params, "TESTKEY");

        assertThat(sign)
                .hasSize(64)
                .matches("[0-9A-F]{64}")
                .isEqualTo("3DBE4E5962857668009B8CDC7EF7665CB2F5A7004CAA02C77559E3DE582622A7");
    }

    @Test
    void verifyShouldAcceptCorrectSignAndRejectWrongSign() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        params.put("sign", NotifyVerifier.sign(params, "K"));

        assertThat(NotifyVerifier.verify(params, "K")).isTrue();

        params.put("sign", "BAD");

        assertThat(NotifyVerifier.verify(params, "K")).isFalse();
    }

    @Test
    void verifyShouldReturnFalseWhenSignMissing() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");

        assertThat(NotifyVerifier.verify(params, "K")).isFalse();
    }
}
