package com.wangguangwu.axflowtenantrouter.core.validator;

import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;

/**
 * 租户特定的请求体验证器接口
 * <p>
 * 负责对请求体进行租户特定的业务规则验证
 *
 * @param <T> 验证的目标类型
 * @author wangguangwu
 */
public interface TenantPayloadValidator<T> {

    /**
     * 验证请求体是否符合租户特定的业务规则
     *
     * @param value 待验证的对象实例
     * @return 验证结果，包含是否通过验证及错误信息
     */
    ValidationResult validate(T value);
}
