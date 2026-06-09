package com.manzhushaka.mq.consumer;

import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MqMessageConsumeExecutor {

    private final MqMessageLedgerService ledgerService;
    private final MqProperties mqProperties;

    public MqMessageConsumeExecutor(MqMessageLedgerService ledgerService, MqProperties mqProperties) {
        this.ledgerService = ledgerService;
        this.mqProperties = mqProperties;
    }

    public void execute(
        MapRecord<String, Object, Object> record,
        String consumerGroup,
        String consumerName,
        MessageHandler handler,
        Consumer<RecordId> acknowledge
    ) {
        try {
            String eventId = parseEventId(record);
            if (eventId == null) {
                return;
            }
            if (ledgerService.isSuccess(eventId)) {
                return;
            }
            ledgerService.markProcessing(
                eventId,
                consumerGroup,
                consumerName,
                mqProperties.getProcessingTimeoutSeconds()
            );
            try {
                handler.handle(record);
            } catch (Exception exception) {
                ledgerService.markFailed(eventId, exception.getMessage());
                return;
            }
            ledgerService.markSuccess(eventId);
        } finally {
            acknowledge.accept(record.getId());
        }
    }

    private String parseEventId(MapRecord<String, Object, Object> record) {
        Object eventId = record.getValue().get("eventId");
        if (eventId == null || String.valueOf(eventId).isBlank()) {
            return null;
        }
        return String.valueOf(eventId);
    }

    @FunctionalInterface
    public interface MessageHandler {
        void handle(MapRecord<String, Object, Object> record) throws Exception;
    }
}
