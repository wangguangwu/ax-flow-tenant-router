package com.wangguangwu.axflowtenantrouter.tenant.validator;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.api.ClaimController;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantAClaimIntakeRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantA特定的理赔申请验证器
 * <p>
 * 负责验证TenantA特定的理赔申请请求，实现租户特定的验证规则
 *
 * @author wangguangwu
 */
@TenantRoute(
        tenant = "TenantA",
        controller = ClaimController.class,
        methods = {"intake"},
        base = ClaimIntakeRequest.class
)
@Component
public class TenantAClaimIntakeValidator implements TenantPayloadValidator<TenantAClaimIntakeRequest> {
    
    @Override
    public ValidationResult validate(TenantAClaimIntakeRequest payload) {
        List<String> errors = new ArrayList<>();
        
        // 示例：insuredName 必须以中文字符开头（演示跨字段/复杂逻辑也可）
        if (payload.getInsuredName() != null && !payload.getInsuredName().matches("^[\\u4e00-\\u9fa5].*")) {
            errors.add("insuredName需要以中文开头(示例)");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.fail(errors);
    }
}
