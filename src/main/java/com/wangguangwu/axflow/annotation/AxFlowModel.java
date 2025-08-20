package com.wangguangwu.axflow.annotation;

import java.lang.annotation.*;

/**
 * {@code @AxFlowModel} 用于标记多租户数据模型的子类。
 * <p>
 * 核心思想：每个业务基类（base model）可以根据不同租户（tenantId）
 * 注册不同的子类实现，在运行时请求解析时会自动根据租户 ID
 * 路由到对应的子类进行反序列化和校验。
 *
 * <h3>属性说明</h3>
 * <ul>
 *   <li>{@link #base()}：
 *   指定该模型所属的基类。默认值为 {@code Void.class}，表示未显式声明。
 *   - 如果未指定，会自动推导：
 *     <ul>
 *       <li>若存在非 {@code Object} 的父类 → 使用最顶层非 {@code Object} 父类作为基类。</li>
 *       <li>若没有父类（或直接继承 {@code Object}）→ 使用当前类自身作为基类。</li>
 *     </ul>
 *   </li>
 *
 *   <li>{@link #value()} ()}：
 *   声明该模型支持的租户 ID 列表。
 *   - 必填，不允许为空。
 *   - 可以包含多个租户。
 *   - 支持按租户维度进行路由。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 *
 * <pre>{@code
 * // 基类
 * public class PaymentRequest {
 *     private String amount;
 * }
 *
 * // 租户 A 的实现
 * @AxFlowModel(tenants = {"TenantA"})
 * public class TenantAPaymentRequest extends PaymentRequest {
 *     @NotBlank(message = "支付宝账号不能为空")
 *     private String alipayAccount;
 * }
 *
 * // 租户 B 的实现
 * @AxFlowModel(tenants = {"TenantB"})
 * public class TenantBPaymentRequest extends PaymentRequest {
 *     @NotBlank(message = "微信 openId 不能为空")
 *     private String wechatOpenId;
 * }
 * }</pre>
 * <p>
 * 在运行时，当请求体传入 JSON 并携带 {@code tenantId = "TenantA"}，
 * 框架会自动选择 {@code TenantAPaymentRequest} 作为目标类型进行反序列化和校验。
 *
 * @author wangguangwu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AxFlowModel {

    /**
     * 绑定的租户 ID 列表。
     * 可以写成 @AxFlowModel("TenantA") 或 @AxFlowModel({"TenantA", "TenantB"})
     */
    String[] value();

    /**
     * 显式指定基类（可选）。
     * 未指定时自动推断父类，若无非 Object 父类，则以自己为基类。
     */
    Class<?> base() default Void.class;
}