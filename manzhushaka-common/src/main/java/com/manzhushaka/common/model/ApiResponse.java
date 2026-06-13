package com.manzhushaka.common.model;

/**
 * 定义 ApiResponse。
 */
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    /**
     * 执行 success 逻辑。
     *
     * @param data data 参数
     * @return 处理结果
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 0;
        response.message = "ok";
        response.data = data;
        return response;
    }

    /**
     * 执行 fail 逻辑。
     *
     * @param code code 参数
     * @param message message 参数
     * @return 处理结果
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        return response;
    }

    /**
     * 返回 code。
     *
     * @return 字段值
     */
    public int getCode() {
        return code;
    }

    /**
     * 返回 message。
     *
     * @return 字段值
     */
    public String getMessage() {
        return message;
    }

    /**
     * 返回 data。
     *
     * @return 字段值
     */
    public T getData() {
        return data;
    }
}
