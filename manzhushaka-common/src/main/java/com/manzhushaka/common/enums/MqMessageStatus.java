package com.manzhushaka.common.enums;

public enum MqMessageStatus {
    INIT,
    PUBLISHED,
    PROCESSING,
    SUCCESS,
    FAIL;

    public boolean allowsManualRetry(boolean processingTimedOut) {
        return switch (this) {
            case INIT, FAIL -> true;
            case PROCESSING -> processingTimedOut;
            case PUBLISHED, SUCCESS -> false;
        };
    }
}
