package com.manzhushaka.common.exception;

/**
 * 定义 BizException。
 */
public class BizException extends RuntimeException {
    private final int code;

    /**
     * 创建 BizException 实例。
     *
     * @param message message 参数
     */
    public BizException(String message) {
        this(500, message);
    }

    /**
     * 创建 BizException 实例。
     *
     * @param code code 参数
     * @param message message 参数
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 返回 code。
     *
     * @return 字段值
     */
    public int getCode() {
        return code;
    }
}
