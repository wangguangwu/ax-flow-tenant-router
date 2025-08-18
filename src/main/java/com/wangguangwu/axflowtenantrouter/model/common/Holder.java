package com.wangguangwu.axflowtenantrouter.model.common;

/**
 * 注册项：目标类型 + Bean实例 + 优先级
 * <p>
 * 用于在注册表中存储绑定器和验证器的信息
 *
 * @param targetType 目标类型
 * @param bean       Bean实例
 * @param order      优先级（数值越小优先级越高）
 * @author wangguangwu
 */
public record Holder(Class<?> targetType, Object bean, int order) {

}
