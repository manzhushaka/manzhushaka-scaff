package com.manzhushaka.common.mybatis.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EncryptedStringTypeHandlerTest {

    @AfterEach
    void tearDown() {
        SensitiveFieldCryptoHolder.clear();
    }

    @Test
    void shouldEncryptParameter() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.encrypt("plain")).thenReturn("v1:cipher");
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        PreparedStatement statement = mock(PreparedStatement.class);
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        handler.setNonNullParameter(statement, 1, "plain", null);

        verify(statement).setString(1, "v1:cipher");
    }

    @Test
    void shouldDecryptResult() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.decrypt("v1:cipher")).thenReturn("plain");
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("email")).thenReturn("v1:cipher");
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        assertEquals("plain", handler.getNullableResult(resultSet, "email"));
    }

    @Test
    void shouldReadPlainResultWhenEncryptorMissing() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("email")).thenReturn("plain");
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        assertEquals("plain", handler.getNullableResult(resultSet, "email"));
    }

    @Test
    void shouldRejectPlainParameterWhenEncryptorMissing() {
        PreparedStatement statement = mock(PreparedStatement.class);
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        assertThrows(IllegalStateException.class,
                () -> handler.setNonNullParameter(statement, 1, "plain", null));
    }

    @Test
    void shouldReturnNullHashWhenEncryptorMissing() {
        assertNull(SensitiveFieldCryptoHolder.hash("plain"));
    }
}