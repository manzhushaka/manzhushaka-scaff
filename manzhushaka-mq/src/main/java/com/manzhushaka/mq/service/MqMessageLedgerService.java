package com.manzhushaka.mq.service;

import com.manzhushaka.mq.core.MqEvent;

import java.time.LocalDateTime;

/**
 * 定义 MqMessageLedgerService 服务能力。
 */
public interface MqMessageLedgerService {

    /**
     * 创建 create Init Record 数据。
     *
     * @param streamKey streamKey 参数
     * @param event event 参数
     */
    void createInitRecord(String streamKey, MqEvent<?> event);

    /**
     * 判断是否 success。
     *
     * @param eventId eventId 标识
     * @return 字段值
     */
    boolean isSuccess(String eventId);

    /**
     * 更新 mark Published 数据。
     *
     * @param eventId eventId 标识
     */
    void markPublished(String eventId);

    /**
     * 更新 mark Processing 数据。
     *
     * @param eventId eventId 标识
     * @param consumerGroup consumerGroup 参数
     * @param consumerName consumerName 参数
     * @param processingTimeoutSeconds processingTimeoutSeconds 参数
     */
    void markProcessing(String eventId, String consumerGroup, String consumerName, int processingTimeoutSeconds);

    /**
     * 更新 mark Success 数据。
     *
     * @param eventId eventId 标识
     */
    void markSuccess(String eventId);

    /**
     * 更新 mark Failed 数据。
     *
     * @param eventId eventId 标识
     * @param errorMessage errorMessage 参数
     */
    void markFailed(String eventId, String errorMessage);

    /**
     * 清理 recycle Timed Out Processing Messages 数据。
     *
     * @param now now 参数
     * @return 处理结果
     */
    int recycleTimedOutProcessingMessages(LocalDateTime now);
}
