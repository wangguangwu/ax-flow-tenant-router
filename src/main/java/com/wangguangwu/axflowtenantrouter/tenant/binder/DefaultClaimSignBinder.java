package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.request.ClaimSignRequest;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认理赔申请绑定器
 * <p>
 * 当没有找到特定租户的绑定器时，使用此默认绑定器
 * 通过设置最低优先级（LOWEST_PRECEDENCE）确保特定租户的绑定器优先被使用
 * 使用简化的 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@TenantBinder(value = "*", order = Ordered.LOWEST_PRECEDENCE)
@Component
public class DefaultClaimSignBinder implements TenantPayloadBinder<ClaimSignRequest> {

    @Override
    public Class<ClaimSignRequest> targetType() {
        return ClaimSignRequest.class;
    }
    
    @Override
    public void afterBind(ClaimSignRequest payload) {
        // 默认绑定后处理逻辑
        // 如果没有特殊处理逻辑，可以留空
    }
}
