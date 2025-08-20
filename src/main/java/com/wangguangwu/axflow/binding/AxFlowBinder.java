package com.wangguangwu.axflow.binding;

/**
 * 绑定器：决定目标子类并在绑定后进行必要的轻量处理。
 *
 * @author wangguangwu
 */
public interface AxFlowBinder {

    /**
     * 是否支持指定的控制器参数基类。
     */
    boolean supportsBaseType(Class<?> baseType);

    /**
     * 解析得到目标子类类型，并返回该子类是否默认启用校验。
     *
     * @param rawBody  原始请求体
     * @param baseType 控制器参数的基类
     */
    Target resolveTarget(byte[] rawBody, Class<?> baseType) throws Exception;

    /**
     * 绑定后轻量处理（归一化/补默认/小派生）。
     */
    default void afterBind(Object value) {
    }

    /**
     * 目标元信息：类型 + 默认是否校验。
     */
    record Target(Class<?> type) {
    }
}
