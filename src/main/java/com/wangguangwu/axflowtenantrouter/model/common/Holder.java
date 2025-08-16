package com.wangguangwu.axflowtenantrouter.model.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 注册项：目标类型 + Bean实例 + 优先级
 * <p>
 * 用于在注册表中存储绑定器和验证器的信息
 *
 * @author wangguangwu
 */
@Getter
@RequiredArgsConstructor
public final class Holder {

    /**
     * 目标类型
     */
    private final Class<?> targetType;
    
    /**
     * Bean实例
     */
    private final Object bean;
    
    /**
     * 优先级（数值越小优先级越高）
     */
    private final int order;
}
