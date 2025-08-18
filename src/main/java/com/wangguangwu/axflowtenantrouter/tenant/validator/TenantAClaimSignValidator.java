package com.wangguangwu.axflowtenantrouter.tenant.validator;

import com.wangguangwu.axflowtenantrouter.annotation.TenantValidator;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantAClaimSignRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantA特定的理赔申请验证器（简化版）
 * <p>
 * 负责验证TenantA特定的理赔申请请求，实现租户特定的验证规则
 * 使用简化的 @TenantValidator 注解
 *
 * @author wangguangwu
 */
@TenantValidator("TenantA")
@Component
public class TenantAClaimSignValidator implements TenantPayloadValidator<TenantAClaimSignRequest> {
    
    @Override
    public ValidationResult validate(TenantAClaimSignRequest payload) {
        List<String> errors = new ArrayList<>();
        
        // 示例：insuredName 必须以中文字符开头（演示跨字段/复杂逻辑也可）
        if (payload.getInsuredName() != null && !payload.getInsuredName().matches("^[\\u4e00-\\u9fa5].*")) {
            errors.add("insuredName需要以中文开头(示例)");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.fail(errors);
    }
}
