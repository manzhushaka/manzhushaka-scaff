package com.manzhushaka.common.context;

/**
 * 定义 DataScopeContext。
 */
public final class DataScopeContext {
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    /**
     * 创建 DataScopeContext 实例。
     */
    private DataScopeContext() {
    }

    /**
     * 执行 set 逻辑。
     *
     * @param scopeSql scopeSql 参数
     */
    public static void set(String scopeSql) {
        HOLDER.set(scopeSql);
    }

    /**
     * 执行 get 逻辑。
     *
     * @return 字段值
     */
    public static String get() {
        return HOLDER.get();
    }

    /**
     * 清理 clear 数据。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
