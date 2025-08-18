package com.wangguangwu.axflowtenantrouter.core.interceptor;

import com.wangguangwu.axflowtenantrouter.core.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器
 *
 * @author wangguangwu
 */
@Component
@SuppressWarnings("all")
public class TenantInterceptor implements HandlerInterceptor {
    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setTenantId(tenantId);
        } else {
            // 可以设置默认租户ID或返回错误
            throw new IllegalArgumentException("Missing required header: " + TENANT_HEADER);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 清理线程局部变量，防止内存泄漏
        TenantContext.clear();
    }
}
