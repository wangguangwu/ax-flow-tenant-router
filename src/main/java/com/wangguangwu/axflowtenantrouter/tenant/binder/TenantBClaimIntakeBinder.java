package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.api.ClaimController;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantBClaimIntakeRequest;
import org.springframework.stereotype.Component;

/**
 * TenantB特定的理赔申请绑定器
 * <p>
 * 负责将请求体绑定到TenantB特定的理赔申请请求类型
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
public class TenantBClaimIntakeBinder implements TenantPayloadBinder<TenantBClaimIntakeRequest> {

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
