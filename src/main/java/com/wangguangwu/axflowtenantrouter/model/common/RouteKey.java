package com.wangguangwu.axflowtenantrouter.model.common;

import lombok.*;

/**
 * 多租户路由匹配的唯一键
 * <p>
 * 用于精确标识一个绑定器/校验器的适用范围
 * 组成：
 * - tenant: 租户ID（支持通配符 *）
 * - key: Controller全类名#方法名（支持通配符 *）
 * - base: 参数基类类型（用于类型匹配）
 *
 * @author wangguangwu
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class RouteKey {

    /**
     * 租户ID（支持通配符 *）
     */
    private final String tenant;

    /**
     * Controller全类名#方法名（支持通配符 *）
     */
    private final String key;

    /**
     * 参数基类
     */
    private final Class<?> base;
}
