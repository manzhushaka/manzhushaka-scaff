package com.manzhushaka.framework.security.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginPrincipalMerchantIdTest {

    @Test
    void builderAndRestoreShouldKeepMerchantId() {
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(10L)
                .deptId(20L)
                .merchantId(30L)
                .username("merchant")
                .build();

        LoginPrincipal restored = LoginPrincipal.restore(principal);

        assertThat(restored.getMerchantId()).isEqualTo(30L);
    }
}
