# AX Flow 多租户路由框架

## 项目概述

AX Flow 多租户路由框架是一个基于Spring Boot的轻量级多租户请求处理框架，允许根据租户ID动态路由和处理HTTP请求。该框架支持将同一API端点的请求根据不同的租户ID映射到不同的处理逻辑和数据模型，实现多租户系统的灵活开发。

## 核心功能

- **动态请求绑定**：根据租户ID将请求体自动绑定到对应的租户特定DTO类
- **租户特定验证**：支持标准Bean验证和租户特定的业务规则验证
- **可扩展架构**：易于添加新租户支持，无需修改现有代码
- **声明式配置**：通过注解轻松配置路由规则和验证逻辑

## 技术栈

- Java 17+
- Spring Boot 3.x
- Spring MVC
- Jakarta Bean Validation API

## 项目结构

```
ax-flow-tenant-router/
├── annotation/                # 核心注解
│   ├── TenantBody.java        # 标记需要租户特定处理的参数
│   └── TenantRoute.java       # 配置租户路由规则
├── api/                       # API控制器
│   └── ClaimController.java   # 示例控制器
├── config/                    # 配置类
│   └── WebMvcConfig.java      # Spring MVC配置
├── core/                      # 核心组件
│   ├── binder/                # 绑定器接口
│   │   └── TenantPayloadBinder.java
│   ├── registry/              # 注册表实现
│   │   ├── AbstractTenantRegistry.java
│   │   ├── TenantBinderRegistry.java
│   │   └── TenantValidatorRegistry.java
│   ├── resolver/              # 参数解析器
│   │   └── TenantBodyArgumentResolver.java
│   └── validator/             # 验证器接口
│       └── TenantPayloadValidator.java
├── model/                     # 数据模型
│   ├── common/                # 通用模型类
│   │   ├── ClaimAcknowledgeRequest.java
│   │   ├── ClaimCloseRequest.java
│   │   ├── ClaimIntakeRequest.java
│   │   ├── Holder.java
│   │   ├── RouteKey.java
│   │   └── ValidationResult.java
│   └── tenant/                # 租户特定模型类
│       ├── TenantAClaimIntakeRequest.java
│       └── TenantBClaimIntakeRequest.java
├── service/                   # 业务服务
│   └── ClaimService.java      # 示例服务
└── tenant/                    # 租户特定实现
    ├── binder/                # 租户特定绑定器
    │   ├── ASignIntakeBinder.java
    │   ├── BSignIntakeBinder.java
    │   └── DefaultIntakeBinder.java
    └── validator/             # 租户特定验证器
        ├── ASignIntakeValidator.java
        └── BSignIntakeValidator.java
```

## 核心组件说明

### 1. 注解

- **@TenantBody**：标记需要进行租户特定处理的控制器方法参数
- **@TenantRoute**：配置租户路由规则，用于注册绑定器和验证器

### 2. 参数解析器

- **TenantBodyArgumentResolver**：核心组件，负责根据租户ID和控制器方法，将请求体动态绑定到对应的子类型，并执行标准Bean验证和租户特定验证

### 3. 绑定器和验证器

- **TenantPayloadBinder**：租户特定的请求体绑定器接口，负责指定请求体应该绑定到哪个具体的子类型
- **TenantPayloadValidator**：租户特定的请求体验证器接口，负责对请求体进行租户特定的业务规则验证

### 4. 注册表

- **TenantBinderRegistry**：管理和查找租户特定的绑定器
- **TenantValidatorRegistry**：管理和查找租户特定的验证器

## 使用指南

### 1. 定义基础请求模型

首先，创建一个通用的基础请求模型类：

```java
@Data
public class ClaimIntakeRequest {
    @NotBlank(message = "caseCode不能为空")
    private String caseCode;
    
    @NotBlank(message = "claimType不能为空")
    private String claimType;
}
```

### 2. 创建租户特定的子类

为每个租户创建特定的请求模型子类：

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantAClaimIntakeRequest extends ClaimIntakeRequest {
    @NotBlank(message = "signCode不能为空")
    private String signCode;
}
```

### 3. 实现租户特定的绑定器

```java
@Component
@TenantRoute(tenantId = "tenant-a", paramType = ClaimIntakeRequest.class)
public class ASignIntakeBinder implements TenantPayloadBinder<TenantAClaimIntakeRequest> {
    @Override
    public Class<TenantAClaimIntakeRequest> targetType() {
        return TenantAClaimIntakeRequest.class;
    }
    
    @Override
    public void postProcess(TenantAClaimIntakeRequest bound) {
        // 可以在这里进行额外的处理
    }
}
```

### 4. 实现租户特定的验证器（可选）

```java
@Component
@TenantRoute(tenantId = "tenant-a", paramType = TenantAClaimIntakeRequest.class)
public class ASignIntakeValidator implements TenantPayloadValidator<TenantAClaimIntakeRequest> {
    @Override
    public ValidationResult validate(TenantAClaimIntakeRequest value) {
        // 实现租户特定的验证逻辑
        if (value.getSignCode().length() < 6) {
            return ValidationResult.error("signCode长度不能小于6");
        }
        return ValidationResult.success();
    }
}
```

### 5. 在控制器中使用

```java
@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {
    private final ClaimService claimService;
    
    @PostMapping("/intake")
    public String intake(@Valid @TenantBody ClaimIntakeRequest request) {
        return claimService.intake(request);
    }
}
```

### 6. 发送请求

发送请求时，需要在请求头中包含租户ID：

```
POST /claims/intake
X-Tenant-Id: tenant-a
Content-Type: application/json

{
  "caseCode": "CASE001",
  "claimType": "VEHICLE",
  "signCode": "ABC123"
}
```

## 扩展指南

### 添加新租户支持

1. 创建新的租户特定请求模型子类
2. 实现对应的租户特定绑定器
3. 可选：实现对应的租户特定验证器
4. 使用`@TenantRoute`注解配置路由规则

无需修改现有代码或配置，框架将自动注册和管理新的租户特定组件。

## 最佳实践

1. 将共享字段和验证规则放在基类中
2. 将租户特定字段和验证规则放在子类中
3. 使用明确的命名约定区分不同租户的实现
4. 为复杂的验证逻辑创建专门的验证器

## 许可证

[待添加]

## 贡献指南

[待添加]
# ax-flow-tenant-router
