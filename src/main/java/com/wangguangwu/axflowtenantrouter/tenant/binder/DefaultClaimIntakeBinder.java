package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.api.ClaimController;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认理赔申请绑定器
 * <p>
 * 当没有找到特定租户的绑定器时，使用此默认绑定器
 * 通过设置最低优先级（LOWEST_PRECEDENCE）确保特定租户的绑定器优先被使用
 *
 * @author wangguangwu
 */
@TenantRoute(
        tenant = "*",
        controller = ClaimController.class,
        methods = {"intake"},
        base = ClaimIntakeRequest.class,
        order = Ordered.LOWEST_PRECEDENCE
)
@Component
public class DefaultClaimIntakeBinder implements TenantPayloadBinder<ClaimIntakeRequest> {

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
