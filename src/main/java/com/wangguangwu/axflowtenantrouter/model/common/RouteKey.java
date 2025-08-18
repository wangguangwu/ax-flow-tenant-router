package com.wangguangwu.axflowtenantrouter.model.common;

/**
 * 多租户路由匹配的唯一键
 * <p>
 * 用于精确标识一个绑定器/校验器的适用范围
 * 组成：
 * - tenant: 租户ID（支持通配符 *）
 * - key: Controller全类名#方法名（支持通配符 *）
 * - base: 参数基类类型（用于类型匹配）
 *
 * @param tenant 租户ID（支持通配符 *）
 * @param key    Controller全类名#方法名（支持通配符 *）
 * @param base   参数基类
 * @author wangguangwu
 */
public record RouteKey(String tenant, String key, Class<?> base) {

}
