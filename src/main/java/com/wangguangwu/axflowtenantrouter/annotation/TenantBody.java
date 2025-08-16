package com.wangguangwu.axflowtenantrouter.annotation;

import java.lang.annotation.*;

/**
 * 租户请求体注解
 * <p>
 * 标记需要进行租户特定处理的控制器方法参数
 * 被此注解标记的参数将由 SimpleTenantBodyArgumentResolver 处理
 * 根据请求中的租户ID动态绑定到对应的子类型
 *
 * @author wangguangwu
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantBody {
    
    /**
     * 租户ID请求头名称
     * <p>
     * 默认为 "X-Tenant-Id"
     */
    String tenantHeader() default "X-Tenant-Id";
}
