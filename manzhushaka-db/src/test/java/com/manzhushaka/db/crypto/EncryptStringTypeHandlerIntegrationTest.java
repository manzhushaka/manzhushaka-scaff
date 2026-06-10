package com.manzhushaka.db.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = EncryptStringTypeHandlerIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:encrypted_type_handler;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "manzhushaka.db.crypto.key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class EncryptStringTypeHandlerIntegrationTest {

    @Autowired
    private DemoEncryptedRecordMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetSchema() {
        jdbcTemplate.execute("drop table if exists demo_encrypted_record");
        jdbcTemplate.execute("""
            create table demo_encrypted_record (
                id bigint primary key,
                username varchar(64) not null,
                mobile_ciphertext varchar(512)
            )
            """);
    }

    @Test
    void storesCiphertextAndReadsPlaintextThroughBaseMapper() {
        DemoEncryptedRecord record = new DemoEncryptedRecord();
        record.setId(1L);
        record.setUsername("alice");
        record.setMobile("13800138000");

        mapper.insert(record);

        String ciphertext = jdbcTemplate.queryForObject(
            "select mobile_ciphertext from demo_encrypted_record where id = ?",
            String.class,
            1L
        );
        DemoEncryptedRecord stored = mapper.selectById(1L);

        assertNotEquals("13800138000", ciphertext);
        assertEquals("13800138000", stored.getMobile());
    }

    @Test
    void decryptsCustomMappedQueryResult() {
        DemoEncryptedRecord record = new DemoEncryptedRecord();
        record.setId(2L);
        record.setUsername("bob");
        record.setMobile("13900139000");
        mapper.insert(record);

        DemoEncryptedRecord stored = mapper.selectByUsername("bob");

        assertEquals("13900139000", stored.getMobile());
    }

    @Test
    void keepsNullValueTransparent() {
        DemoEncryptedRecord record = new DemoEncryptedRecord();
        record.setId(3L);
        record.setUsername("carol");
        record.setMobile(null);
        mapper.insert(record);

        String ciphertext = jdbcTemplate.queryForObject(
            "select mobile_ciphertext from demo_encrypted_record where id = ?",
            String.class,
            3L
        );
        DemoEncryptedRecord stored = mapper.selectById(3L);

        assertNull(ciphertext);
        assertNull(stored.getMobile());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan(basePackageClasses = DemoEncryptedRecordMapper.class, annotationClass = Mapper.class)
    @Import(DbCryptoConfig.class)
    static class TestApplication {
    }
}
