package com.wangguangwu.axflowtenantrouter.core.resolver;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBody;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.core.registry.SimpleTenantBinderRegistry;
import com.wangguangwu.axflowtenantrouter.core.registry.SimpleTenantValidatorRegistry;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
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

import java.util.Objects;
import java.util.Optional;

/**
 * 简化的多租户请求体参数解析器
 * <p>
 * 根据租户ID，将请求体动态绑定到对应的子类型，
 * 并执行标准Bean验证和租户特定验证
 *
 * @author wangguangwu
 */
@Component
@RequiredArgsConstructor
public class SimpleTenantBodyArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 默认租户ID请求头名称
     */
    private static final String DEFAULT_TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 标准Bean验证器
     */
    private final SmartValidator validator;
    
    /**
     * JSON转换器
     */
    private final MappingJackson2HttpMessageConverter jackson;
    
    /**
     * 简化的租户绑定器注册表
     */
    private final SimpleTenantBinderRegistry binderRegistry;
    
    /**
     * 简化的租户验证器注册表
     */
    private final SimpleTenantValidatorRegistry validatorRegistry;

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

        // 获取TenantBody注解
        TenantBody tenantBodyAnnotation = parameter.getParameterAnnotation(TenantBody.class);
        String tenantHeader = tenantBodyAnnotation != null ? 
                tenantBodyAnnotation.tenantHeader() : DEFAULT_TENANT_ID_HEADER;

        // 获取租户ID
        final String tenantId = servletRequest.getHeader(tenantHeader);
        if (tenantId == null || tenantId.isBlank()) {
            throw new HttpMessageNotReadableException("缺少租户ID请求头: " + tenantHeader, input);
        }

        // 获取参数基类类型
        final Class<?> baseType = parameter.getParameterType();

        // 尝试直接反序列化为基类
        Object baseValue;
        try {
            baseValue = jackson.read(baseType, baseType, input);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(
                    String.format("请求体反序列化失败: baseType=%s, tenant=%s, error=%s",
                            baseType.getSimpleName(), tenantId, e.getMessage()),
                    e, input);
        }

        // 查找匹配的绑定器
        Holder binderHolder = binderRegistry.findBinder(tenantId, baseType)
                .orElse(null);

        // 如果没有找到绑定器，直接使用基类对象
        if (binderHolder == null) {
            return validateAndReturn(parameter, baseValue, tenantId, baseType);
        }

        // 获取目标类型
        final Class<?> targetType = binderHolder.getTargetType();

        // 反序列化请求体为目标类型
        final Object value;
        try {
            // 重新读取请求体，转换为目标类型
            value = jackson.read(targetType, targetType, input);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(
                    String.format("请求体反序列化失败: targetType=%s, tenant=%s, error=%s",
                            targetType.getSimpleName(), tenantId, e.getMessage()),
                    e, input);
        }

        // 验证并执行绑定后处理
        Object result = validateAndReturn(parameter, value, tenantId, targetType);

        // 执行绑定后处理
        @SuppressWarnings("unchecked")
        TenantPayloadBinder<Object> binder = (TenantPayloadBinder<Object>) binderHolder.getBean();
        try {
            binder.afterBind(result);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(
                    String.format("绑定后处理失败: targetType=%s, tenant=%s, error=%s",
                            targetType.getSimpleName(), tenantId, e.getMessage()),
                    e, input);
        }

        return result;
    }

    /**
     * 验证对象并返回
     * 
     * @param parameter 方法参数
     * @param value 对象值
     * @param tenantId 租户ID
     * @param valueType 对象类型
     * @return 验证后的对象
     * @throws MethodArgumentNotValidException 如果验证失败
     */
    private Object validateAndReturn(MethodParameter parameter, Object value, String tenantId, Class<?> valueType) 
            throws MethodArgumentNotValidException {
        // 获取参数名称
        final String objectName = Optional.ofNullable(parameter.getParameterName()).orElse("tenantBody");
        
        // 执行标准Bean验证
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(value, objectName);
        validator.validate(value, errors);

        // 执行租户特定验证
        validatorRegistry.findValidator(tenantId, valueType)
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

        return value;
    }
}
