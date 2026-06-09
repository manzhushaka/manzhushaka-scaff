package com.manzhushaka.common.context;

public final class DataScopeContext {
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void set(String scopeSql) {
        HOLDER.set(scopeSql);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
