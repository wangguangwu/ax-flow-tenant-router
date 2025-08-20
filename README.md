
# AxFlow 多租户路由框架

> 基于 Spring Boot 的多租户“请求体自动路由 + 统一校验”解决方案。  
> 通过 **`@AxFlow`（方法级）** + **`@AxFlowModel`（模型级）** 两个注解，按租户 ID 自动选择目标模型子类并完成校验。

---

## 🚀 快速开始

```bash
mvn -q spring-boot:run
# 健康检查
curl -s http://localhost:8080/payment/ping
```

**示例调用（TenantA → AliPay）**
```bash
curl -sS -X POST http://localhost:8080/payment/submit \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: TenantA' \
  -d '{"amount":"100.00","sellerId":"A-SELLER-001","appId":"A-APP-001"}'
```

成功返回（统一包裹为 `ApiResult`）：
```json
{"code":200,"message":"OK","data":{"amount":"100.00","sellerId":"A-SELLER-001","appId":"A-APP-001"}}
```

---

## 🧩 核心注解（已在工程中实现）

### `@AxFlowModel`
标注在 **租户特定模型子类** 上。声明该子类对应的租户集合，以及（可选的）**基类**。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AxFlowModel {
    String[] value();           // 必填，支持的租户列表，如 {"TenantA"} 或 {"TenantA","TenantB"}
    Class<?> base() default Void.class; // 可选，显式指定基类；未指定将自动推断父类，否则用自身作为基类
}
```

> **自动推断规则**：若存在非 `Object` 父类，则使用最顶层非 `Object` 父类作为基类；若没有父类，则使用**当前类自己**作为基类键。

### `@AxFlow`
标注在 **Controller 方法** 上，用于接管请求体解析、可选校验，以及租户访问控制（白/黑名单）。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AxFlow {
    boolean validate() default true;         // 是否启用校验（JSR-303 + 自定义 Validator）
    Class<?>[] groups() default {};         // JSR-303 分组
    String[] allowedTenants() default {};   // 白名单（空数组表示不限制）
    String[] deniedTenants() default {};    // 黑名单（空数组表示不限制）
    int paramIndex() default -1;            // 绑定的参数索引（不常用）
    String paramName() default "";          // 绑定的参数名（不常用）
    boolean bodyRequired() default true;    // 是否必须有请求体
}
```

---

## 🧠 工作流程（文字版）

1. `TenantInterceptor` 从请求头 **`X-Tenant-Id`** 读取租户 ID 写入 `TenantContext`。  
2. `AxFlowArgumentResolver` 捕获带 `@AxFlow` 的方法，读取请求体字节。  
3. `AxFlowSubtypeRegistry` 根据 **(baseType, tenantId)** 找到实际子类；若找不到，使用 **baseType** 本身。  
4. `AxFlowBinderFactory` 调用 **JacksonBinder** 将请求体反序列化为目标子类实例。  
5. 若 `validate = true`：`AxFlowValidationService` 先跑 JSR-303，再按类型执行自定义 `AxFlowValidator`。  
6. 控制器返回 `ApiResult`；异常统一由 `GlobalExceptionHandler` 包装。

> **注意**：白名单/黑名单检查在解析前执行；黑名单优先。

---

## 🆕 如何引入 **新租户**（不引入新逻辑，按当前工程约定）

> 假设我们已有基类 `PaymentRequest`，现在要新增租户 **TenantC**，并添加其专属请求模型与校验。

### ✅ 步骤 0：确认扫描包
`application.yml` 中配置了模型扫描包（默认已经指向示例包）：
```yaml
axflow:
  scan-base-packages: com.wangguangwu.axflow.sample.model
```
> 将你的新租户模型类放在这里或追加自定义包路径（逗号分隔）。

### ✅ 步骤 1：创建租户模型子类（继承基类）
在 `com.wangguangwu.axflow.sample.model` 下新增：
```java
@Data
@EqualsAndHashCode(callSuper = true)
@AxFlowModel("TenantC")
public class UnionPayRequest extends PaymentRequest {

    @NotBlank(message = "UnionPay: merId 不能为空")
    private String merId;

    @NotBlank(message = "UnionPay: appId 不能为空")
    private String appId;
}
```
> 这里未显式指定 `base`，框架会自动使用 `PaymentRequest` 作为基类。

### ✅ （可选）步骤 2：新增租户业务校验器
```java
@Component
@Order(100)
public class UnionPayValidator implements AxFlowValidator<UnionPayRequest> {

    @Override
    public Class<UnionPayRequest> targetType() { return UnionPayRequest.class; }

    @Override
    public AxFlowValidationResult validate(UnionPayRequest v) {
        if (!v.getMerId().startsWith("U")) {
            return AxFlowValidationResult.fail("UnionPay: merId 必须以 U 开头");
        }
        if (Objects.equals(v.getMerId(), v.getAppId())) {
            return AxFlowValidationResult.fail("UnionPay: merId 和 appId 不能相同");
        }
        return AxFlowValidationResult.ok();
    }
}
```
> 建议每个租户模型都配一个简单业务校验器，便于明确化租户规则。

### ✅ 步骤 3：控制器上开放租户访问（白名单）
找到 `PaymentController#submitPayment`：
```java
@AxFlow(allowedTenants = {"TenantA", "TenantB"})
@PostMapping("/submit")
public ApiResult<?> submitPayment(@RequestBody(required = false) PaymentRequest request) {
    return ApiResult.success(request);
}
```
添加 TenantC：
```java
@AxFlow(allowedTenants = {"TenantA", "TenantB", "TenantC"})
```
> 如果希望“所有租户”都能访问，可以把 `allowedTenants` 留空（表示不限制），或写成 `{"*"}`（显式允许全部）。

### ✅ 步骤 4：发起请求验证
```bash
curl -sS -X POST http://localhost:8080/payment/submit \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: TenantC' \
  -d '{"amount":"300.00","merId":"U-MER-001","appId":"U-APP-001"}' | jq
```
**预期**：`data` 字段为 `UnionPayRequest` 的 JSON；校验通过返回 `200`，若不满足 `@NotBlank` 或自定义规则，返回 `400` 的 `ApiResult`。

---

## 🧭 多种场景指引

### 场景 A：**非继承** 的模型
如果你的模型类 **没有继承任何非 `Object` 父类**，且你仍希望在 `Controller` 的参数上使用某个“基类”来接收，那就需要在注解里**显式指定 `base`**：
```java
@Data
@AxFlowModel(value = "TenantX", base = PaymentRequest.class) // 手动指定基类
public class TenantXPayment implements /* 无父类 */ Serializable {
    @NotBlank private String customField;
}
```
> 否则，框架会把“当前类自己”作为基类键，无法匹配到控制器参数的 `PaymentRequest`。

### 场景 B：一个租户为 **多个基类** 提供不同实现
- 在各自子类上写 `@AxFlowModel("TenantZ")` 即可（自动按各自的 `base` 建表）。

### 场景 C：扩展/替换绑定策略（一般不需要）
- 默认 **`JacksonBinder`** 已满足大多数 JSON 反序列化需求；如需特殊格式，可实现 `AxFlowBinder`，并让 `supportsBaseType` 返回 `true` 时才纳入候选。**注意**：如果同一个 `baseType` 下出现 **多个候选 Binder**，工厂会抛冲突异常（这是既有行为，避免歧义）。

### 场景 D：修改租户头名称
- 目前常量在 `TenantInterceptor.HEADER_TENANT = "X-Tenant-Id"`；如需修改，请同步更新客户端与文档。

---

## 🛠 测试脚本（只校验 ApiResult.code）
项目根目录有 `test-payment.sh`，执行：
```bash
chmod +x test-payment.sh
./test-payment.sh
```
- PASS/FAIL 汇总明确。  
- 只以 `ApiResult.code` 为断言来源（不再纠结 HTTP 状态）。

---

## 📂 目录结构
```
src/main/java/com/wangguangwu/axflow/
├── annotation/     # 注解定义（@AxFlow, @AxFlowModel）
├── binding/        # 绑定器与工厂（JacksonBinder）
├── common/         # ApiResult 与全局异常
├── config/         # MVC 配置（拦截器与参数解析器注册）
├── context/        # TenantContext
├── registry/       # 子类注册中心（扫描 @AxFlowModel）
├── sample/         # 示例模型/校验器/控制器
├── validation/     # 校验接口与服务
└── web/            # 租户拦截器、参数解析器
```

---

## ❗ 常见坑位
- **缺少租户头**：拦截器抛 `IllegalArgumentException`，统一返回 `ApiResult(code=400)`。  
- **未在白名单**：`@AxFlow` 方法层面拒绝（黑名单优先，其次白名单）。  
- **找不到子类映射**：会退回使用 **baseType** 反序列化；检查是否漏了 `@AxFlowModel` 或扫描包未覆盖。  
- **非继承模型匹配失败**：请在 `@AxFlowModel(base=...)` 中显式指定控制器参数使用的基类。  
- **多 Binder 冲突**：同 `baseType` 的候选超过 1 个会报错，确保只有一个生效。

---

## 📝 版权 & 作者
- 作者：**wangguangwu**  
- 许可证：按你项目实际选择（当前示例未附带 License）
