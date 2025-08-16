package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 租户绑定器注册表
 * <p>
 * 管理 TenantPayloadBinder 的注册与查询
 *
 * @author wangguangwu
 */
@Component
public class TenantBinderRegistry extends AbstractTenantRegistry<TenantPayloadBinder<?>> {

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
    protected Class<?> extractTargetType(Object bean) {
        return ((TenantPayloadBinder<?>) bean).targetType();
    }

    @Override
    protected String getRegistryName() {
        return "Binder";
    }

    /**
     * 查找适用于指定租户、路由键和基类的绑定器
     *
     * @param tenant 租户ID
     * @param key    路由键（类名#方法名）
     * @param base   基类类型
     * @return 匹配的绑定器
     */
    public Optional<Holder> findBinder(String tenant, String key, Class<?> base) {
        return super.find(tenant, key, base, null);
    }
}
