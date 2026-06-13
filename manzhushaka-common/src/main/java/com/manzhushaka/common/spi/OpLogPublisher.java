package com.manzhushaka.common.spi;

import com.manzhushaka.common.model.OpLogRecord;

/**
 * 定义 OpLogPublisher。
 */
public interface OpLogPublisher {
    void publish(OpLogRecord record);
}
