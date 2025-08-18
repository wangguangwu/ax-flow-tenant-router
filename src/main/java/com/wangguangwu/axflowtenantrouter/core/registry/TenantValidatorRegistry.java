package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.annotation.TenantValidator;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * 简化的租户验证器注册表
 * <p>
 * 管理 TenantPayloadValidator 的注册与查询，使用 @TenantValidator 注解
 *
 * @author wangguangwu
 */
@Component
public class TenantValidatorRegistry extends TenantRegistry<TenantPayloadValidator<?>> {

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
        // 泛型擦除下直接返回原接口类型即可
        return (Class<TenantPayloadValidator<?>>) (Class<?>) TenantPayloadValidator.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationType() {
        return TenantValidator.class;
    }

    @Override
    protected Class<?> extractTargetType(Object bean) {
        // 通过反射获取泛型参数类型
        return getValidatorTargetType((TenantPayloadValidator<?>) bean);
    }

    @Override
    protected String extractTenant(Annotation annotation) {
        return ((TenantValidator) annotation).value();
    }

    @Override
    protected int extractOrder(Annotation annotation) {
        return ((TenantValidator) annotation).order();
    }

    @Override
    protected String getRegistryName() {
        return "Validator";
    }

    /**
     * 获取验证器的目标类型
     *
     * @param validator 验证器实例
     * @return 目标类型
     */
    private Class<?> getValidatorTargetType(TenantPayloadValidator<?> validator) {
        // 这里可以通过反射获取泛型参数类型
        // 简化实现，通过验证方法的参数类型推断
        try {
            return validator.getClass().getMethod("validate", Object.class)
                    .getParameterTypes()[0];
        } catch (NoSuchMethodException e) {
            // 如果无法通过反射获取，可以提供一个默认实现
            // 例如，可以要求验证器实现一个 targetType() 方法
            throw new IllegalStateException("无法确定验证器的目标类型: " + validator.getClass().getName(), e);
        }
    }

    /**
     * 查找适用于指定租户和目标类型的验证器
     *
     * @param tenant     租户ID
     * @param targetType 目标类型
     * @return 匹配的验证器
     */
    public Optional<Holder> findValidator(String tenant, Class<?> targetType) {
        return super.find(tenant, targetType);
    }
}
