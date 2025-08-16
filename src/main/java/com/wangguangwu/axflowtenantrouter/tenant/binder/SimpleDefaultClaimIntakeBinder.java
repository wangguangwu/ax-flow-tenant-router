package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.request.ClaimIntakeRequest;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认理赔申请绑定器（简化版）
 * <p>
 * 当没有找到特定租户的绑定器时，使用此默认绑定器
 * 通过设置最低优先级（LOWEST_PRECEDENCE）确保特定租户的绑定器优先被使用
 * 使用简化的 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@TenantBinder(value = "*", order = Ordered.LOWEST_PRECEDENCE)
@Component
public class SimpleDefaultClaimIntakeBinder implements TenantPayloadBinder<ClaimIntakeRequest> {

    @Override
    public Class<ClaimIntakeRequest> targetType() {
        return ClaimIntakeRequest.class;
    }
    
    @Override
    public void afterBind(ClaimIntakeRequest payload) {
        // 默认绑定后处理逻辑
        // 如果没有特殊处理逻辑，可以留空
    }
}
