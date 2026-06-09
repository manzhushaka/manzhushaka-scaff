package com.manzhushaka.common.spi;

import com.manzhushaka.common.model.OpLogRecord;

public interface OpLogPublisher {
    void publish(OpLogRecord record);
}
