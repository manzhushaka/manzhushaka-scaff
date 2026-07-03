package com.manzhushaka.biz.pii.integration;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiMerchantProfileMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayOrderMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayQrcodeMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayQrcodeTaxItemMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiRefundRecordMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiTaxItemMapper;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.MerchantProfileRepositoryImpl;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.PayOrderRepositoryImpl;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.PayQrcodeRepositoryImpl;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.PayQrcodeTaxItemRepositoryImpl;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.RefundRecordRepositoryImpl;
import com.manzhushaka.biz.pii.infrastructure.persistence.repository.TaxItemRepositoryImpl;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class PiiIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("pii_integration")
            .withUsername("pii")
            .withPassword("pii");

    @Test
    void piiIntegrationTestShouldPersistPaymentInvoiceRefundAndReverseFlow() throws Exception {
        SqlSessionFactory sqlSessionFactory = buildSqlSessionFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            MerchantProfileRepositoryImpl merchantRepository = new MerchantProfileRepositoryImpl(
                    session.getMapper(PiiMerchantProfileMapper.class));
            TaxItemRepositoryImpl taxItemRepository = new TaxItemRepositoryImpl(
                    session.getMapper(PiiTaxItemMapper.class));
            PayQrcodeRepositoryImpl qrcodeRepository = new PayQrcodeRepositoryImpl(
                    session.getMapper(PiiPayQrcodeMapper.class));
            PayQrcodeTaxItemRepositoryImpl relationRepository = new PayQrcodeTaxItemRepositoryImpl(
                    session.getMapper(PiiPayQrcodeTaxItemMapper.class));
            PayOrderRepositoryImpl orderRepository = new PayOrderRepositoryImpl(
                    session.getMapper(PiiPayOrderMapper.class));
            RefundRecordRepositoryImpl refundRepository = new RefundRecordRepositoryImpl(
                    session.getMapper(PiiRefundRecordMapper.class));

            LocalDateTime now = LocalDateTime.now();
            Long merchantId = merchantRepository.insert(merchant(now));
            Long taxItemId = taxItemRepository.insert(taxItem(now));
            Long qrcodeId = qrcodeRepository.insert(qrcode(merchantId, now));
            relationRepository.insert(relation(qrcodeId, taxItemId));

            PayQrcodeTaxItem binding = relationRepository.findByQrcodeIdAndTaxItemId(qrcodeId, taxItemId).orElseThrow();
            assertThat(binding.getDefaultAmount()).isEqualTo(8800L);

            Long orderId = orderRepository.insert(order(merchantId, qrcodeId, taxItemId, now));
            assertThat(orderRepository.findByOutTradeNoAndToken("PII260703150000000001", "token-001"))
                    .get()
                    .extracting(PayOrder::getPayStatus, PayOrder::getInvoiceStatus)
                    .containsExactly("PENDING", "NOT_ISSUED");

            orderRepository.updatePayStatus(orderId, "PAID", "ums-trade-001", now.plusSeconds(1));
            orderRepository.updateInvoiceStatus(orderId, "ISSUED", "INV0001", "CODE0001",
                    "https://example.test/invoice.pdf", now.plusSeconds(2));

            Long refundId = refundRepository.insert(refund(merchantId, orderId, now.plusSeconds(3)));
            refundRepository.updateStatus(refundId, "SUCCESS", "ums-refund-001", now.plusSeconds(4));
            orderRepository.updateRefundAmountAndStatus(orderId, 8800L, "REFUNDED");
            orderRepository.updateInvoiceReverseStatus(orderId, "REVERSED", now.plusSeconds(5));
            session.commit();

            PayOrder finalOrder = orderRepository.findById(orderId).orElseThrow();
            RefundRecord finalRefund = refundRepository.findByOutRefundNo("PIR260703150000000001").orElseThrow();

            assertThat(finalOrder.getPayStatus()).isEqualTo("REFUNDED");
            assertThat(finalOrder.getRefundAmount()).isEqualTo(8800L);
            assertThat(finalOrder.getInvoiceStatus()).isEqualTo("REVERSED");
            assertThat(finalOrder.getInvoiceNo()).isEqualTo("INV0001");
            assertThat(finalRefund.getStatus()).isEqualTo("SUCCESS");
            assertThat(refundRepository.sumSuccessAmountByPayOrderId(orderId)).isEqualTo(8800L);
            assertThat(orderRepository.sumAmountByMerchantAndStatusBetween(
                    merchantId, List.of("REFUNDED"), now.minusMinutes(1), now.plusMinutes(1))).isEqualTo(8800L);
        }
    }

    private SqlSessionFactory buildSqlSessionFactory() throws Exception {
        DataSource dataSource = new UnpooledDataSource(
                MYSQL.getDriverClassName(), MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
        try (Connection connection = dataSource.getConnection();
             BufferedReader schema = Files.newBufferedReader(repoFile("sql/pii_schema.sql"), StandardCharsets.UTF_8)) {
            ScriptRunner runner = new ScriptRunner(connection);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(schema);
        }

        Environment environment = new Environment("pii-integration", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(PiiMerchantProfileMapper.class);
        configuration.addMapper(PiiTaxItemMapper.class);
        configuration.addMapper(PiiPayQrcodeMapper.class);
        configuration.addMapper(PiiPayQrcodeTaxItemMapper.class);
        configuration.addMapper(PiiPayOrderMapper.class);
        configuration.addMapper(PiiRefundRecordMapper.class);
        addMapperXml(configuration, "mapper/pii/PiiMerchantProfileMapper.xml");
        addMapperXml(configuration, "mapper/pii/PiiTaxItemMapper.xml");
        addMapperXml(configuration, "mapper/pii/PiiPayQrcodeMapper.xml");
        addMapperXml(configuration, "mapper/pii/PiiPayQrcodeTaxItemMapper.xml");
        addMapperXml(configuration, "mapper/pii/PiiPayOrderMapper.xml");
        addMapperXml(configuration, "mapper/pii/PiiRefundRecordMapper.xml");
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private Path repoFile(String relativePath) {
        Path userDir = Path.of(System.getProperty("user.dir"));
        Path direct = userDir.resolve(relativePath);
        if (Files.exists(direct)) {
            return direct;
        }
        return userDir.getParent().resolve(relativePath);
    }

    private void addMapperXml(Configuration configuration, String resource) throws Exception {
        try (Reader mapper = Resources.getResourceAsReader(resource)) {
            new org.apache.ibatis.builder.xml.XMLMapperBuilder(
                    mapper, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    private MerchantProfile merchant(LocalDateTime now) {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setDeptId(2001L);
        merchant.setMerchantName("海南测试商户");
        merchant.setUmsMerchantId("898000000000001");
        merchant.setUmsTerminalId("00000001");
        merchant.setUmsPaySignKeyEnc("pay-key");
        merchant.setUmsInvoiceSignKeyEnc("invoice-key");
        merchant.setInvoiceMsgSrc("PII");
        merchant.setInvoiceSellerName("海南测试商户");
        merchant.setInvoiceSellerTaxCode("91460000TEST001");
        merchant.setNotifyUrl("https://example.test/invoice/notify");
        merchant.setStatus(1);
        merchant.setCreateTime(now);
        merchant.setUpdateTime(now);
        return merchant;
    }

    private TaxItem taxItem(LocalDateTime now) {
        TaxItem taxItem = new TaxItem();
        taxItem.setTaxItemCode("3040501000000000000");
        taxItem.setName("餐饮服务");
        taxItem.setCategory("生活服务");
        taxItem.setTaxRate(new BigDecimal("6.00"));
        taxItem.setSort(1);
        taxItem.setStatus(1);
        taxItem.setCreateTime(now);
        taxItem.setUpdateTime(now);
        return taxItem;
    }

    private PayQrcode qrcode(Long merchantId, LocalDateTime now) {
        PayQrcode qrcode = new PayQrcode();
        qrcode.setMerchantId(merchantId);
        qrcode.setQrcodeCode("QR2607030001");
        qrcode.setQrcodeUrl("https://example.test/pay/QR2607030001");
        qrcode.setName("收银台二维码");
        qrcode.setStatus(1);
        qrcode.setExpireTime(now.plusDays(1));
        qrcode.setCreateTime(now);
        qrcode.setUpdateTime(now);
        return qrcode;
    }

    private PayQrcodeTaxItem relation(Long qrcodeId, Long taxItemId) {
        PayQrcodeTaxItem relation = new PayQrcodeTaxItem();
        relation.setQrcodeId(qrcodeId);
        relation.setTaxItemId(taxItemId);
        relation.setDefaultAmount(8800L);
        return relation;
    }

    private PayOrder order(Long merchantId, Long qrcodeId, Long taxItemId, LocalDateTime now) {
        PayOrder order = new PayOrder();
        order.setMerchantId(merchantId);
        order.setQrcodeId(qrcodeId);
        order.setTaxItemId(taxItemId);
        order.setOutTradeNo("PII260703150000000001");
        order.setUmsMerOrderDate("20260703");
        order.setAmount(8800L);
        order.setBuyerName("测试企业");
        order.setBuyerTaxCode("91460000BUYER001");
        order.setBuyerEmail("buyer@example.test");
        order.setBuyerMobile("13800000000");
        order.setBuyerOpenid("openid-001");
        order.setPayStatus("PENDING");
        order.setPayNotifyStatus("INIT");
        order.setRefundAmount(0L);
        order.setInvoiceStatus("NOT_ISSUED");
        order.setOrderToken("token-001");
        order.setWechatAppid("wx-test");
        order.setClientIp("127.0.0.1");
        order.setCreateTime(now);
        order.setUpdateTime(now);
        return order;
    }

    private RefundRecord refund(Long merchantId, Long orderId, LocalDateTime now) {
        RefundRecord refund = new RefundRecord();
        refund.setMerchantId(merchantId);
        refund.setPayOrderId(orderId);
        refund.setOutRefundNo("PIR260703150000000001");
        refund.setAmount(8800L);
        refund.setReason("整单退款");
        refund.setStatus("PENDING");
        refund.setOperatorId(1L);
        refund.setTriggerInvoiceReverse(1);
        refund.setCreateTime(now);
        refund.setUpdateTime(now);
        return refund;
    }
}
