package com.wangguangwu.axflowtenantrouter.core.binder;

/**
 * 租户特定的请求体绑定器接口
 * <p>
 * 负责指定请求体应该绑定到哪个具体的子类型，并提供绑定后的处理钩子
 *
 * @param <T> 目标绑定类型
 * @author wangguangwu
 */
public interface TenantPayloadBinder<T> {

    /**
     * 获取目标绑定类型
     *
     * @return 目标类型的Class对象
     */
    Class<T> targetType();

    /**
     * 绑定后处理钩子方法
     * <p>
     * 可用于数据补充、派生字段计算等操作
     *
     * @param value 已绑定的对象实例
     */
    default void afterBind(T value) {
        // 默认空实现
    }
}
