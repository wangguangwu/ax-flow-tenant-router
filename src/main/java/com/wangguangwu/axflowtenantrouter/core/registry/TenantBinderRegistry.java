package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.tenant.binder.NoopBinder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * 简化的租户绑定器注册表
 * <p>
 * 管理 TenantPayloadBinder 的注册与查询，使用 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@Component
public class TenantBinderRegistry extends TenantRegistry<TenantPayloadBinder<?>> {

    /**
     * 构造函数
     *
     * @param applicationContext Spring应用上下文
     */
    public TenantBinderRegistry(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<TenantPayloadBinder<?>> getBeanType() {
        // 泛型擦除下直接返回原接口类型即可
        return (Class<TenantPayloadBinder<?>>) (Class<?>) TenantPayloadBinder.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return TenantBinder.class;
    }

    @Override
    protected Class<?> extractTargetType(Object bean) {
        return ((TenantPayloadBinder<?>) bean).targetType();
    }

    @Override
    protected String extractTenant(Annotation annotation) {
        return ((TenantBinder) annotation).value();
    }

    @Override
    protected int extractOrder(Annotation annotation) {
        return ((TenantBinder) annotation).order();
    }

    @Override
    protected String getRegistryName() {
        return "Binder";
    }

    /**
     * 查找适用于指定租户和目标类型的绑定器
     * <p>
     * 找不到专用 binder 时，返回一个以 baseType 为 targetType 的兜底 Holder
     *
     * @param tenant   租户ID
     * @param baseType 目标类型
     * @return 匹配的绑定器
     */
    public Optional<Holder> resolveOrDefault(String tenant, Class<?> baseType) {
        Optional<Holder> found = super.find(tenant, baseType);
        if (found.isPresent()) {
            return found;
        }

        // 拿到 NoopBinder 实例
        TenantPayloadBinder<?> noop = applicationContext.getBean(NoopBinder.class);
        // 关键：兜底时把 targetType 设为 baseType（而不是 NoopBinder 的 Object.class）
        Holder fallback = new Holder(baseType, noop, Ordered.LOWEST_PRECEDENCE);
        return Optional.of(fallback);
    }
}
