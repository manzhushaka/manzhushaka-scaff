package com.manzhushaka.biz.pii.infrastructure.persistence.support;

import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PiiTenantInterceptorTest {

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
}
