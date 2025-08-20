package com.wangguangwu.axflow.annotation;

import java.lang.annotation.*;

/**
 * 方法级入口注解：接管请求体解析、（可选）统一校验，并控制租户访问策略与目标参数选择。
 *
 * <p>解析器逻辑：</p>
 * <ol>
 *   <li>选择被绑定的参数（paramIndex > paramName > 自动推断唯一复杂对象）</li>
 *   <li>读取 Body → 租户路由绑定子类 → 反序列化</li>
 *   <li>是否校验：方法级 validate 优先，否则用子类的 validateByDefault</li>
 * </ol>
 *
 * @author wangguangwu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AxFlow {

    /**
     * 是否启用统一校验（方法级覆盖）。默认 true。
     */
    boolean validate() default true;

    /**
     * JSR-303 分组（等价于 @Validated(groups=...)）。
     */
    Class<?>[] groups() default {};

    /**
     * 允许访问的租户白名单（空数组表示不限制）。
     */
    String[] allowedTenants() default {};

    /**
     * 禁止访问的租户黑名单（空数组表示不限制）。
     */
    String[] deniedTenants() default {};

    /**
     * 作为请求体的参数索引（0基）。默认 -1 表示未指定。
     */
    int paramIndex() default -1;

    /**
     * 作为请求体的参数名称（需 -parameters 编译）。默认空表示未指定。
     */
    String paramName() default "";

    /**
     * 请求体是否必填（true 时空体抛 400）。默认 true。
     */
    boolean bodyRequired() default true;
}
