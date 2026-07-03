package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 发票邮件网关配置测试。
 *
 * @author manzhushaka
 * @date 2026-07-03
 */
class InvoiceEmailGatewayConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(InvoiceEmailGatewayConfig.class);

    /**
     * 未提供真实邮件网关时，应创建日志兜底实现。
     */
    @Test
    void shouldProvideLoggingGatewayWhenMissingCustomGateway() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(InvoiceEmailGateway.class);
            assertThat(context).hasSingleBean(LoggingInvoiceEmailGateway.class);
        });
    }

    /**
     * 已提供真实邮件网关时，应跳过日志兜底实现。
     */
    @Test
    void shouldBackOffWhenCustomGatewayExists() {
        InvoiceEmailGateway customGateway = request -> {
        };

        contextRunner.withBean("customInvoiceEmailGateway", InvoiceEmailGateway.class, () -> customGateway)
                .run(context -> {
                    assertThat(context).hasSingleBean(InvoiceEmailGateway.class);
                    assertThat(context).doesNotHaveBean(LoggingInvoiceEmailGateway.class);
                    assertThat(context.getBean(InvoiceEmailGateway.class)).isSameAs(customGateway);
                });
    }
}
