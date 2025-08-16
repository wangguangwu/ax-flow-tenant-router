package com.wangguangwu.axflowtenantrouter.core.resolver;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBody;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.core.registry.TenantBinderRegistry;
import com.wangguangwu.axflowtenantrouter.core.registry.TenantValidatorRegistry;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * 多租户请求体参数解析器
 * <p>
 * 根据租户ID和控制器方法，将请求体动态绑定到对应的子类型，
 * 并执行标准Bean验证和租户特定验证
 *
 * @author wangguangwu
 */
@Component
@RequiredArgsConstructor
public class TenantBodyArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 租户ID请求头名称
     */
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 标准Bean验证器
     */
    private final SmartValidator validator;
    
    /**
     * JSON转换器
     */
    private final MappingJackson2HttpMessageConverter jackson;
    
    /**
     * 租户绑定器注册表
     */
    private final TenantBinderRegistry binderRegistry;
    
    /**
     * 租户验证器注册表
     */
    private final TenantValidatorRegistry validatorRegistry;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TenantBody.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        // 获取原始Servlet请求
        HttpServletRequest servletRequest =
                Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class), "请求对象不能为空");

        // 包装为Spring的HttpInputMessage
        ServletServerHttpRequest input = new ServletServerHttpRequest(servletRequest);

        // 获取租户ID
        final String tenantId = servletRequest.getHeader(TENANT_ID_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            throw new HttpMessageNotReadableException("缺少租户ID请求头: " + TENANT_ID_HEADER, input);
        }

        // 构建路由键: ControllerFQN#methodName
        Method method = Objects.requireNonNull(parameter.getMethod(), "方法不能为空");
        final String routeKey = method.getDeclaringClass().getName() + "#" + method.getName();

        // 获取参数基类类型
        final Class<?> baseType = parameter.getParameterType();

        // 查找匹配的绑定器
        Holder binderHolder = binderRegistry.findBinder(tenantId, routeKey, baseType)
                .orElseThrow(() -> new HttpMessageNotReadableException(
                        String.format("未找到匹配的绑定器: tenant=%s, key=%s, base=%s",
                                tenantId, routeKey, baseType.getSimpleName()),
                        input));

        // 获取目标类型
        final Class<?> targetType = binderHolder.getTargetType();

        // 反序列化请求体
        final Object value;
        try {
            value = jackson.read(targetType, targetType, input);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(
                    String.format("请求体反序列化失败: targetType=%s, tenant=%s, key=%s, error=%s",
                            targetType.getSimpleName(), tenantId, routeKey, e.getMessage()),
                    e, input);
        }

        // 获取参数名称
        final String objectName = Optional.ofNullable(parameter.getParameterName()).orElse("tenantBody");
        
        // 执行标准Bean验证
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(value, objectName);
        validator.validate(value, errors);

        // 执行租户特定验证
        validatorRegistry.findValidator(tenantId, routeKey, baseType, targetType)
                .ifPresent(validatorHolder -> {
                    @SuppressWarnings("unchecked")
                    TenantPayloadValidator<Object> tenantValidator =
                            (TenantPayloadValidator<Object>) validatorHolder.getBean();
                    ValidationResult validationResult = tenantValidator.validate(value);
                    if (!validationResult.isValid()) {
                        // 将租户特定验证错误添加到绑定结果
                        for (String errorMessage : validationResult.getErrors()) {
                            errors.addError(new ObjectError(objectName, errorMessage));
                        }
                    }
                });

        // 如果有验证错误，抛出异常
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, errors);
        }

        // 执行绑定后处理
        @SuppressWarnings("unchecked")
        TenantPayloadBinder<Object> binder = (TenantPayloadBinder<Object>) binderHolder.getBean();
        try {
            binder.afterBind(value);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(
                    String.format("绑定后处理失败: targetType=%s, tenant=%s, key=%s, error=%s",
                            targetType.getSimpleName(), tenantId, routeKey, e.getMessage()),
                    e, input);
        }

        // 返回处理后的对象
        return value;
    }
}
