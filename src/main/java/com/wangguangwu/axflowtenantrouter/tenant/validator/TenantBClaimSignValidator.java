package com.wangguangwu.axflowtenantrouter.tenant.validator;

import com.wangguangwu.axflowtenantrouter.annotation.TenantValidator;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantBClaimSignRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantB特定的理赔申请验证器（简化版）
 * <p>
 * 负责验证TenantB特定的理赔申请请求，实现租户特定的验证规则
 * 使用简化的 @TenantValidator 注解
 *
 * @author wangguangwu
 */
@TenantValidator("TenantB")
@Component
public class TenantBClaimSignValidator implements TenantPayloadValidator<TenantBClaimSignRequest> {
    
    @Override
    public ValidationResult validate(TenantBClaimSignRequest payload) {
        List<String> errors = new ArrayList<>();
        
        // TenantB特定的验证逻辑
        if (payload.getPolicyNo() != null && payload.getPolicyNo().length() < 10) {
            errors.add("保单号长度不能小于10位(示例)");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.fail(errors);
    }
}
