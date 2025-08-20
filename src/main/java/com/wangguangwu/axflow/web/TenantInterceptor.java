package com.wangguangwu.axflow.web;

import com.wangguangwu.axflow.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 在控制器前读取租户ID并写入 TenantContext；缺失时抛出 403。
 *
 * @author wangguangwu
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    public static final String HEADER_TENANT = "X-Tenant-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IllegalAccessException {
        String tenantId = request.getHeader(HEADER_TENANT);
        if (tenantId == null || tenantId.isBlank()) {
            // 抛给 MVC 异常链，交由 @RestControllerAdvice 转成 ApiResult
            throw new IllegalAccessException("请求头缺少租户ID");
        }
        TenantContext.setTenantId(tenantId.trim());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        TenantContext.clear();
    }
}
