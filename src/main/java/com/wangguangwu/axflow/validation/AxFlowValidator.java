package com.wangguangwu.axflow.validation;

/**
 * 业务校验器：按 targetType 选择，支持多实现 + @Order 排序。
 *
 * @param <T> 实体类型
 * @author wangguangwu
 */
public interface AxFlowValidator<T> {

    /**
     * 此校验器面向的实体类型（用于 supports 判断）。
     */
    Class<T> targetType();

    /**
     * 是否支持当前实体类型。
     */
    default boolean supports(Class<?> actualType) {
        return targetType().isAssignableFrom(actualType);
    }

    /**
     * 执行业务校验。
     */
    AxFlowValidationResult validate(T value);
}
