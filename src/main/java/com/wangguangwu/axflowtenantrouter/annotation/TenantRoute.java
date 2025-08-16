package com.wangguangwu.axflowtenantrouter.annotation;

import java.lang.annotation.*;

/**
 * 配置租户路由规则
 * <p>
 * 用于标记绑定器和验证器，指定其适用的租户、控制器、方法和基类
 *
 * @author wangguangwu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantRoute {

    /**
     * 租户ID，支持通配符"*"表示适用于所有租户
     */
    String tenant();

    /**
     * 适用的控制器类，默认为Object.class表示通配
     */
    Class<?> controller() default Object.class;

    /**
     * 适用的方法名列表，支持通配符"*"表示适用于所有方法
     */
    String[] methods() default {"*"};

    /**
     * 参数基类类型，用于类型匹配
     */
    Class<?> base();

    /**
     * 优先级，数值越小优先级越高，用于解决冲突
     */
    int order() default 0;
}
