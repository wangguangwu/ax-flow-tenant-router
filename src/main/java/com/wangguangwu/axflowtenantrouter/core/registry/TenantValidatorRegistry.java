package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * 租户验证器注册表
 * <p>
 * 管理 TenantPayloadValidator 的注册与查询
 *
 * @author wangguangwu
 */
@Component
public class TenantValidatorRegistry extends AbstractTenantRegistry<TenantPayloadValidator<?>> {

    /**
     * 构造函数
     *
     * @param applicationContext Spring应用上下文
     */
    public TenantValidatorRegistry(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<TenantPayloadValidator<?>> getBeanType() {
        return (Class<TenantPayloadValidator<?>>) (Class<?>) TenantPayloadValidator.class;
    }

    @Override
    protected Class<?> extractTargetType(Object bean) {
        // 通过 validate(T) 的参数类型推断 T
        return Arrays.stream(bean.getClass().getMethods())
                .filter(m -> m.getName().equals("validate"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No validate method found in " + bean.getClass()))
                .getParameterTypes()[0];
    }

    @Override
    protected String getRegistryName() {
        return "Validator";
    }

    /**
     * 查找适用于指定租户、路由键和基类的验证器
     * <p>
     * 验证器需要额外匹配目标类型
     *
     * @param tenant     租户ID
     * @param key        路由键（类名#方法名）
     * @param base       基类类型
     * @param targetType 目标类型
     * @return 匹配的验证器
     */
    public Optional<Holder> findValidator(String tenant, String key, Class<?> base, Class<?> targetType) {
        return super.find(tenant, key, base, targetType);
    }
}
