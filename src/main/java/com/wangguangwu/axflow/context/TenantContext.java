package com.wangguangwu.axflow.context;

/**
 * 多租户上下文（ThreadLocal）。
 *
 * @author wangguangwu
 */
public final class TenantContext {

    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(String tenantId) {
        TL.set(tenantId);
    }

    public static String getTenantId() {
        return TL.get();
    }

    public static void clear() {
        TL.remove();
    }
}
