package com.wangguangwu.axflow.binding;

import com.wangguangwu.axflow.context.TenantContext;
import com.wangguangwu.axflow.registry.AxFlowSubtypeRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 基于类型级 @AxFlowModel 的租户感知绑定器：
 * 仅依据租户ID路由子类，不解析报文字段。
 *
 * @author wangguangwu
 */
@Component
@Order(100)
public class AxFlowAnnotatedBinder implements AxFlowBinder {

    private final AxFlowSubtypeRegistry registry;

    public AxFlowAnnotatedBinder(AxFlowSubtypeRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean supportsBaseType(Class<?> baseType) {
        return true;
    }

    @Override
    public Target resolveTarget(byte[] rawBody, Class<?> baseType) {
        String tenant = Objects.toString(TenantContext.getTenantId(), "");
        var meta = registry.resolve(baseType, tenant);
        if (meta.isEmpty()) {
            return new Target(baseType);
        }
        var m = meta.get();
        return new Target(m.subtype());
    }
}
