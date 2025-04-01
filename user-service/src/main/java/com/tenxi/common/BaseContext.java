package com.tenxi.common;

public class BaseContext {
    public static ThreadLocal<Long> context = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        context.set(id);
    }
    public static Long getCurrentId() {
        return context.get();
    }
    public static void clearCurrentId() {
        context.remove();
    }
}
