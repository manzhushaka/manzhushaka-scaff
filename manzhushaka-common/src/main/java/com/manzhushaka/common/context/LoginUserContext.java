package com.manzhushaka.common.context;

/**
 * 定义 LoginUserContext。
 */
public final class LoginUserContext {
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /**
     * 创建 LoginUserContext 实例。
     */
    private LoginUserContext() {
    }

    /**
     * 执行 set 逻辑。
     *
     * @param loginUser loginUser 参数
     */
    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /**
     * 执行 get 逻辑。
     *
     * @return 字段值
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 清理 clear 数据。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
