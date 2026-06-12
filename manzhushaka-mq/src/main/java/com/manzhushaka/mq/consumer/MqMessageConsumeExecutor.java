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

    /**
     * 执行消息消费并在成功后确认消息。
     *
     * @param record 消息记录
     * @param consumerGroup 消费组
     * @param consumerName 消费者名称
     * @param handler 消费处理器
     * @param acknowledge 消息确认回调
     */
    public void execute(
        MapRecord<String, Object, Object> record,
        String consumerGroup,
        String consumerName,
        MessageHandler handler,
        Consumer<RecordId> acknowledge
    ) {
        String eventId = parseEventId(record);
        if (eventId == null) {
            acknowledge.accept(record.getId());
            return;
        }
        if (ledgerService.isSuccess(eventId)) {
            acknowledge.accept(record.getId());
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
        acknowledge.accept(record.getId());
    }

    /**
     * 从消息体中解析事件 ID。
     *
     * @param record 消息记录
     * @return 事件 ID；不存在时返回 null
     */
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
