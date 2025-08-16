package com.wangguangwu.axflowtenantrouter.tenant.validator;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.api.ClaimController;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantBClaimIntakeRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantB特定的理赔申请验证器
 * <p>
 * 负责验证TenantB特定的理赔申请请求，实现租户特定的验证规则
 *
 * @author wangguangwu
 */
@TenantRoute(
        tenant = "TenantB",
        controller = ClaimController.class,
        methods = {"intake"},
        base = ClaimIntakeRequest.class
)
@Component
public class TenantBClaimIntakeValidator implements TenantPayloadValidator<TenantBClaimIntakeRequest> {

    @Override
    public ValidationResult validate(TenantBClaimIntakeRequest payload) {
        List<String> errors = new ArrayList<>();
        
        // 验证优先级不能为负数
        if (payload.getPriority() != null && payload.getPriority() < 0) {
            errors.add("priority不能为负数");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.fail(errors);
    }
}
