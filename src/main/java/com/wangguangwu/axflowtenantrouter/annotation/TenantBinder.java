package com.wangguangwu.axflowtenantrouter.annotation;

import java.lang.annotation.*;

/**
 * 简化的租户绑定器注解
 * <p>
 * 用于标记绑定器，只需指定其适用的租户ID
 *
 * @author wangguangwu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantBinder {

    /**
     * 租户ID，支持通配符"*"表示适用于所有租户
     */
    String value();
    
    /**
     * 优先级，数值越小优先级越高，用于解决冲突
     */
    int order() default 0;
}
