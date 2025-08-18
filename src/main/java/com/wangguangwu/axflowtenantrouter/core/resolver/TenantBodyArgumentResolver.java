package com.wangguangwu.axflowtenantrouter.core.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wangguangwu.axflowtenantrouter.core.binder.TenantPayloadBinder;
import com.wangguangwu.axflowtenantrouter.core.context.TenantContext;
import com.wangguangwu.axflowtenantrouter.core.registry.TenantBinderRegistry;
import com.wangguangwu.axflowtenantrouter.core.registry.TenantValidatorRegistry;
import com.wangguangwu.axflowtenantrouter.core.validator.TenantPayloadValidator;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.model.common.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
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
public class TenantBodyArgumentResolver implements HandlerMethodArgumentResolver {

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
    private final TenantBinderRegistry binderRegistry;

    /**
     * 简化的租户验证器注册表
     */
    private final TenantValidatorRegistry validatorRegistry;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest servletRequest =
                Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class), "请求对象不能为空");

        // 一次性把请求体读成字节，避免多次读流
        byte[] body = StreamUtils.copyToByteArray(servletRequest.getInputStream());
        if (body.length == 0) {
            throw new IllegalArgumentException("请求体为空");
        }

        String tenantId = TenantContext.getTenantId();
        Class<?> baseType = parameter.getParameterType();
        ObjectMapper mapper = jackson.getObjectMapper();

        // 统一从注册表拿 Holder（专用或兜底 Noop）
        Holder binderHolder = binderRegistry.resolveOrDefault(tenantId, baseType)
                .orElseThrow(() -> new IllegalStateException("无法解析绑定器"));

        Class<?> targetType = binderHolder.targetType();

        // 直接按 targetType 反序列化（只反一次）
        final Object value;
        try {
            value = mapper.readValue(body, targetType);
        } catch (Exception e) {
            String raw = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            throw new IllegalArgumentException(
                    String.format("请求体反序列化失败: targetType=%s, tenant=%s, error=%s, raw=%s",
                            targetType.getSimpleName(), tenantId, e.getMessage(), raw), e);
        }

        // 校验（标准 + 租户特定）
        Object result = validateAndReturn(parameter, value, tenantId, targetType);

        // 绑定后处理（统一调用）
        @SuppressWarnings("unchecked")
        TenantPayloadBinder<Object> binder = (TenantPayloadBinder<Object>) binderHolder.bean();
        try {
            binder.afterBind(result);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("绑定后处理失败: targetType=%s, tenant=%s, error=%s",
                            targetType.getSimpleName(), tenantId, e.getMessage()), e);
        }

        return result;
    }

    /**
     * 验证对象并返回
     *
     * @param parameter 方法参数
     * @param value     对象值
     * @param tenantId  租户ID
     * @param valueType 对象类型
     * @return 验证后的对象
     * @throws MethodArgumentNotValidException 如果验证失败
     */
    private Object validateAndReturn(
            MethodParameter parameter, Object value, String tenantId, Class<?> valueType)
            throws MethodArgumentNotValidException {

        final String objectName = Optional.ofNullable(parameter.getParameterName())
                .filter(s -> !s.isBlank())
                .orElseGet(() -> valueType != null ? valueType.getSimpleName() : "requestBody");

        // 统一的错误收集器
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(value, objectName);

        // 1) 先跑标准 Bean 校验（JSR-303 注解：@NotNull、@Size 等）
        validator.validate(value, errors);

        // 2) 再叠加租户特定校验（可能是 NoopValidator → 什么都不做）
        validatorRegistry.resolveOrDefault(tenantId, valueType)
                .ifPresent(validatorHolder -> {
                    @SuppressWarnings("unchecked")
                    TenantPayloadValidator<Object> tenantValidator =
                            (TenantPayloadValidator<Object>) validatorHolder.bean();
                    ValidationResult vr = tenantValidator.validate(value);
                    if (!vr.isValid()) {
                        vr.getErrors().forEach(msg -> errors.addError(new ObjectError(objectName, msg)));
                    }
                });

        // 3) 统一抛错（便于 @ControllerAdvice 处理）
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, errors);
        }
        return value;
    }
}
