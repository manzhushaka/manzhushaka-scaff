package com.manzhushaka.web.controller.pii;

import com.manzhushaka.common.annotation.Anonymous;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class PiiSecurityContractTest {

    @Test
    void consumerAndCallbackControllersShouldAllowAnonymousAccess() throws Exception {
        assertThat(AnonPayController.class.getAnnotation(Anonymous.class)).isNotNull();
        assertThat(AnonOrderController.class.getAnnotation(Anonymous.class)).isNotNull();
        assertThat(AnonQrcodeQueryController.class.getAnnotation(Anonymous.class)).isNotNull();
        assertThat(PayNotifyController.class.getAnnotation(Anonymous.class)).isNotNull();
        assertThat(InvoiceNotifyController.class.getAnnotation(Anonymous.class)).isNotNull();
        assertThat(method(RefundController.class, "notify", String.class, String.class)
                .getAnnotation(Anonymous.class)).isNotNull();
    }

    @Test
    void managementControllersShouldDeclareExpectedPermissions() throws Exception {
        assertPreAuthorize(RefundController.class, "add", "biz:refund:add", com.manzhushaka.web.dto.pii.CreateRefundRequest.class);
        assertPreAuthorize(BiController.class, "data", "biz:bi:dashboard", com.manzhushaka.web.dto.pii.BiDashboardRequest.class);
        assertPreAuthorize(BiController.class, "deptAggregate", "biz:bi:dashboard", com.manzhushaka.web.dto.pii.BiDeptAggregateRequest.class);
    }

    private void assertPreAuthorize(Class<?> controller, String methodName, String permission, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        PreAuthorize annotation = method(controller, methodName, parameterTypes).getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("@ss.hasPermi('" + permission + "')");
    }

    private Method method(Class<?> controller, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return controller.getMethod(methodName, parameterTypes);
    }
}
