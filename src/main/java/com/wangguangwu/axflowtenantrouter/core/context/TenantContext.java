package com.wangguangwu.axflowtenantrouter.core.context;

/**
 * 租户上下文
 *
 * @author wangguangwu
 */
public class TenantContext {

    private static final ThreadLocal<String> TENANT_MAP = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(String tenantId) {
        TENANT_MAP.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_MAP.get();
    }

    public static void clear() {
        TENANT_MAP.remove();
    }
}
