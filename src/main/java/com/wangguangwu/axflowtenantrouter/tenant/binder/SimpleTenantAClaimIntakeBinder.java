package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantAClaimIntakeRequest;
import org.springframework.stereotype.Component;

/**
 * TenantA特定的理赔申请绑定器（简化版）
 * <p>
 * 负责将请求体绑定到TenantA特定的理赔申请请求类型
 * 使用简化的 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@TenantBinder("TenantA")
@Component
public class SimpleTenantAClaimIntakeBinder implements TenantPayloadBinder<TenantAClaimIntakeRequest> {

    @Override
    public Class<TenantAClaimIntakeRequest> targetType() {
        return TenantAClaimIntakeRequest.class;
    }
    
    @Override
    public void afterBind(TenantAClaimIntakeRequest payload) {
        // TenantA特定的绑定后处理逻辑
        // 如果没有特殊处理逻辑，可以留空
    }
}
