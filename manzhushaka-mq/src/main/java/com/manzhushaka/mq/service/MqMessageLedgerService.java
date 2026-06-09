package com.manzhushaka.mq.service;

import com.manzhushaka.mq.core.MqEvent;

public interface MqMessageLedgerService {

    void createInitRecord(String streamKey, MqEvent<?> event);

    boolean isSuccess(String eventId);

    void markPublished(String eventId);

    void markProcessing(String eventId, String consumerGroup, String consumerName, int processingTimeoutSeconds);

    void markSuccess(String eventId);

    void markFailed(String eventId, String errorMessage);
}
