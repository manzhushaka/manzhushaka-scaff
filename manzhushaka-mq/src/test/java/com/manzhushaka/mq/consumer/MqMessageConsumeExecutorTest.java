package com.manzhushaka.mq.consumer;

import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MqMessageConsumeExecutorTest {

    @Test
    void executeShouldMarkProcessingThenMarkSuccessAndAcknowledgeWhenHandlerSucceeds() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(false);

        executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge);

        InOrder inOrder = inOrder(ledgerService, handler, acknowledge);
        inOrder.verify(ledgerService).isSuccess("event-001");
        inOrder.verify(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        inOrder.verify(handler).handle(record);
        inOrder.verify(ledgerService).markSuccess("event-001");
        inOrder.verify(acknowledge).accept(record.getId());
        verify(ledgerService, never()).markFailed("event-001", "handler failed");
    }

    @Test
    void executeShouldMarkFailedAndAcknowledgeWhenHandlerThrowsException() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(false);
        doThrow(new IllegalStateException("handler failed")).when(handler).handle(record);

        executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge);

        InOrder inOrder = inOrder(ledgerService, handler, acknowledge);
        inOrder.verify(ledgerService).isSuccess("event-001");
        inOrder.verify(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        inOrder.verify(handler).handle(record);
        inOrder.verify(ledgerService).markFailed("event-001", "handler failed");
        inOrder.verify(acknowledge).accept(record.getId());
        verify(ledgerService, never()).markSuccess("event-001");
    }

    @Test
    void executeShouldOnlyAcknowledgeWhenLedgerAlreadyMarkedSuccess() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(true);

        executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge);

        verify(acknowledge).accept(record.getId());
        verify(ledgerService, never()).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        verify(ledgerService, never()).markSuccess("event-001");
        verify(ledgerService, never()).markFailed("event-001", "handler failed");
        verify(handler, never()).handle(record);
    }

    @Test
    void executeShouldOnlyAcknowledgeWhenEventIdIsMissing() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecordWithoutEventId();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);

        executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge);

        verify(acknowledge).accept(record.getId());
        verifyNoInteractions(ledgerService, handler);
    }

    @Test
    void executeShouldThrowAndNotAcknowledgeWhenMarkProcessingThrowsException() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(false);
        IllegalStateException expected = new IllegalStateException("mark processing failed");
        doThrow(expected).when(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);

        IllegalStateException actual = assertThrows(
            IllegalStateException.class,
            () -> executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge)
        );

        assertSame(expected, actual);
        verify(ledgerService).isSuccess("event-001");
        verify(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        verify(handler, never()).handle(record);
        verify(acknowledge, never()).accept(record.getId());
    }

    @Test
    void executeShouldThrowMarkFailedExceptionAndNotAcknowledgeWhenHandlerAndMarkFailedBothThrow() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(false);
        doThrow(new IllegalStateException("handler failed")).when(handler).handle(record);
        IllegalStateException expected = new IllegalStateException("mark failed persistence error");
        doThrow(expected).when(ledgerService).markFailed("event-001", "handler failed");

        IllegalStateException actual = assertThrows(
            IllegalStateException.class,
            () -> executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge)
        );

        assertSame(expected, actual);
        InOrder inOrder = inOrder(ledgerService, handler);
        inOrder.verify(ledgerService).isSuccess("event-001");
        inOrder.verify(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        inOrder.verify(handler).handle(record);
        inOrder.verify(ledgerService).markFailed("event-001", "handler failed");
        verify(acknowledge, never()).accept(record.getId());
        verify(ledgerService, never()).markSuccess("event-001");
    }

    @Test
    void executeShouldThrowAndNotAcknowledgeWhenMarkSuccessThrowsException() throws Exception {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, mqProperties);
        MapRecord<String, Object, Object> record = buildRecord();
        @SuppressWarnings("unchecked")
        MqMessageConsumeExecutor.MessageHandler handler = mock(MqMessageConsumeExecutor.MessageHandler.class);
        @SuppressWarnings("unchecked")
        Consumer<RecordId> acknowledge = mock(Consumer.class);
        when(ledgerService.isSuccess("event-001")).thenReturn(false);
        IllegalStateException expected = new IllegalStateException("mark success failed");
        doThrow(expected).when(ledgerService).markSuccess("event-001");

        IllegalStateException actual = assertThrows(
            IllegalStateException.class,
            () -> executor.execute(record, "oplog-group", "consumer-a", handler, acknowledge)
        );

        assertSame(expected, actual);
        InOrder inOrder = inOrder(ledgerService, handler, acknowledge);
        inOrder.verify(ledgerService).isSuccess("event-001");
        inOrder.verify(ledgerService).markProcessing("event-001", "oplog-group", "consumer-a", 300);
        inOrder.verify(handler).handle(record);
        inOrder.verify(ledgerService).markSuccess("event-001");
        verify(ledgerService, never()).markFailed("event-001", "mark success failed");
        verify(acknowledge, never()).accept(record.getId());
    }

    @SuppressWarnings("unchecked")
    private MapRecord<String, Object, Object> buildRecord() {
        MapRecord<String, Object, Object> record = mock(MapRecord.class);
        when(record.getId()).thenReturn(RecordId.of("1717910400000-0"));
        when(record.getValue()).thenReturn(Map.of("eventId", "event-001", "payload", "{\"ok\":true}"));
        return record;
    }

    @SuppressWarnings("unchecked")
    private MapRecord<String, Object, Object> buildRecordWithoutEventId() {
        MapRecord<String, Object, Object> record = mock(MapRecord.class);
        when(record.getId()).thenReturn(RecordId.of("1717910400000-1"));
        when(record.getValue()).thenReturn(Map.of("payload", "{\"ok\":true}"));
        return record;
    }
}
