package com.wangguangwu.axflowtenantrouter.tenant.binder;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBinder;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author wangguangwu
 */
@TenantBinder(value = "*", order = Ordered.LOWEST_PRECEDENCE)
@Component
public class NoopBinder implements TenantPayloadBinder<Object> {

    @Override
    public Class<Object> targetType() {
        // 这里不返回具体类型，由注册表在兜底时把 targetType 替换成当前 baseType
        return Object.class;
    }

    @Override
    public void afterBind(Object payload) {
        // 无操作
    }
}
