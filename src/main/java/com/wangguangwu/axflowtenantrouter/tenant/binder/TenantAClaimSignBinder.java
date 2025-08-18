package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.tenant.TenantAClaimSignRequest;
import org.springframework.stereotype.Component;

/**
 * TenantA特定的理赔申请绑定器
 * <p>
 * 负责将请求体绑定到TenantA特定的理赔申请请求类型
 * 使用简化的 @TenantBinder 注解
 *
 * @author wangguangwu
 */
@TenantBinder("TenantA")
@Component
public class TenantAClaimSignBinder implements TenantPayloadBinder<TenantAClaimSignRequest> {

    @Override
    public Class<TenantAClaimSignRequest> targetType() {
        return TenantAClaimSignRequest.class;
    }
    
    @Override
    public void afterBind(TenantAClaimSignRequest payload) {
        // TenantA特定的绑定后处理逻辑
        // 如果没有特殊处理逻辑，可以留空
    }
}
