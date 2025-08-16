package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantBClaimIntakeRequest;
import org.springframework.stereotype.Component;

/**
 * TenantB特定的理赔申请绑定器（简化版）
 * <p>
 * 负责将请求体绑定到TenantB特定的理赔申请请求类型
 * 使用简化的 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@TenantBinder("TenantB")
@Component
public class SimpleTenantBClaimIntakeBinder implements TenantPayloadBinder<TenantBClaimIntakeRequest> {

    @Override
    public Class<TenantBClaimIntakeRequest> targetType() {
        return TenantBClaimIntakeRequest.class;
    }
    
    @Override
    public void afterBind(TenantBClaimIntakeRequest payload) {
        // TenantB特定的绑定后处理逻辑
        // 如果没有特殊处理逻辑，可以留空
    }
}
