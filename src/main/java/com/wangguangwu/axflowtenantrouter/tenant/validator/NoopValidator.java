package com.wangguangwu.axflowtenantrouter.tenant.validator;

import com.wangguangwu.axflowtenantrouter.annotation.TenantValidator;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 兜底校验器
 *
 * @author wangguangwu
 */
@TenantValidator(value = "*", order = Ordered.LOWEST_PRECEDENCE)
@Component
public class NoopValidator implements TenantPayloadValidator<Object> {

    @Override
    public ValidationResult validate(Object value) {
        // 永远通过
        return ValidationResult.success();
    }
}
