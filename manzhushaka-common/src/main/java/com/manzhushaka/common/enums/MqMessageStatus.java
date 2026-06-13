package com.manzhushaka.common.enums;

/**
 * 定义 MqMessageStatus 枚举值。
 */
public enum MqMessageStatus {
    INIT,
    PUBLISHED,
    PROCESSING,
    SUCCESS,
    FAIL;

    /**
     * 执行 allows Manual Retry 逻辑。
     *
     * @param processingTimedOut processingTimedOut 参数
     * @return 处理结果
     */
    public boolean allowsManualRetry(boolean processingTimedOut) {
        return switch (this) {
            case INIT, FAIL -> true;
            case PUBLISHED, PROCESSING -> processingTimedOut;
            case SUCCESS -> false;
        };
    }
}
