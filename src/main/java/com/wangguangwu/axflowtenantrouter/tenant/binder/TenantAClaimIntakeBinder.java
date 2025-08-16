package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.api.ClaimController;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantAClaimIntakeRequest;
import org.springframework.stereotype.Component;

/**
 * TenantA特定的理赔申请绑定器
 * <p>
 * 负责将请求体绑定到TenantA特定的理赔申请请求类型
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
public class TenantAClaimIntakeBinder implements TenantPayloadBinder<TenantAClaimIntakeRequest> {

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
