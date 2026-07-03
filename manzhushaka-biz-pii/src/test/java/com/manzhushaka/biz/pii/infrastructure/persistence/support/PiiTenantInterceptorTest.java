package com.manzhushaka.biz.pii.infrastructure.persistence.support;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PiiTenantInterceptorTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void interceptorIsWired() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));
        assertNotNull(interceptor);
    }

    @Test
    void injectsMerchantFilterForTenantTableWithoutWhere() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));

        String sql = interceptor.injectTenantFilter("select * from pii_pay_order order by create_time desc", 100L);

        assertEquals("select * from pii_pay_order where merchant_id = 100 order by create_time desc", sql);
    }

    @Test
    void injectsMerchantFilterForTenantTableWithWhere() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));

        String sql = interceptor.injectTenantFilter("select * from pii_pay_order where pay_status = 'PAID' limit 10", 100L);

        assertEquals("select * from pii_pay_order where pay_status = 'PAID' and merchant_id = 100 limit 10", sql);
    }

    @Test
    void skipsPlatformTableAndExistingMerchantFilter() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));

        assertEquals("select * from pii_tax_item", interceptor.injectTenantFilter("select * from pii_tax_item", 100L));
        assertEquals("select * from pii_pay_order where merchant_id = ?",
                interceptor.injectTenantFilter("select * from pii_pay_order where merchant_id = ?", 100L));
    }

    @Test
    void currentMerchantIdShouldUsePrincipalMerchantId() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));
        setPrincipal(LoginPrincipal.builder()
                .userId(2L)
                .merchantId(300L)
                .roleKeys(Set.of("merchant"))
                .build());

        Long merchantId = ReflectionTestUtils.invokeMethod(interceptor, "currentMerchantId");

        assertEquals(300L, merchantId);
    }

    @Test
    void currentMerchantIdShouldResolveMerchantByDeptId() {
        MerchantProfileRepository repository = mock(MerchantProfileRepository.class);
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(301L);
        when(repository.findByDeptId(2001L)).thenReturn(Optional.of(merchant));
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(repository);
        setPrincipal(LoginPrincipal.builder()
                .userId(2L)
                .deptId(2001L)
                .roleKeys(Set.of("merchant"))
                .build());

        Long merchantId = ReflectionTestUtils.invokeMethod(interceptor, "currentMerchantId");

        assertEquals(301L, merchantId);
        verify(repository).findByDeptId(2001L);
    }

    @Test
    void currentMerchantIdShouldSkipAdminAndOperator() {
        PiiTenantInterceptor interceptor = new PiiTenantInterceptor(mock(MerchantProfileRepository.class));
        setPrincipal(LoginPrincipal.builder().userId(1L).merchantId(300L).roleKeys(Set.of("admin")).build());
        assertNull(ReflectionTestUtils.invokeMethod(interceptor, "currentMerchantId"));

        setPrincipal(LoginPrincipal.builder().userId(2L).merchantId(300L).roleKeys(Set.of("operator")).build());
        assertNull(ReflectionTestUtils.invokeMethod(interceptor, "currentMerchantId"));
    }

    private void setPrincipal(LoginPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
