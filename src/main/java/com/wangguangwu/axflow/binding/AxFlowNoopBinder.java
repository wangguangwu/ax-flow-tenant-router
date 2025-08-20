package com.wangguangwu.axflow.binding;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 无操作绑定器：不改变类型，默认校验=true。
 * 不参与候选竞争，仅在无候选时由工厂兜底返回。
 *
 * @author wangguangwu
 */
@Component
@Order()
public class AxFlowNoopBinder implements AxFlowBinder {

    @Override
    public boolean supportsBaseType(Class<?> baseType) {
        return false;
    }

    @Override
    public Target resolveTarget(byte[] rawBody, Class<?> baseType) {
        return new Target(baseType);
    }
}
