package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 发票邮件网关配置。
 *
 * @author manzhushaka
 * @date 2026-07-03
 */
@Configuration
public class InvoiceEmailGatewayConfig {

    /**
     * 未接入真实邮件网关时，提供日志兜底实现，避免消息处理器无法启动。
     *
     * @return 发票邮件网关
     */
    @Bean
    @ConditionalOnMissingBean(InvoiceEmailGateway.class)
    public InvoiceEmailGateway invoiceEmailGateway() {
        return new LoggingInvoiceEmailGateway();
    }
}
