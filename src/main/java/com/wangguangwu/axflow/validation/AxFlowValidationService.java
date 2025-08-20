package com.wangguangwu.axflow.validation;

import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一校验服务：先 JSR-303（可带 groups），再按类型筛选业务校验器（支持 @Order）。
 *
 * @author wangguangwu
 */
@Component
public class AxFlowValidationService {

    private final ApplicationContext applicationContext;
    private final SmartValidator smartValidator;

    public AxFlowValidationService(ApplicationContext applicationContext, SmartValidator smartValidator) {
        this.applicationContext = applicationContext;
        this.smartValidator = smartValidator;
    }

    private final Map<Class<?>, List<AxFlowValidator<?>>> cache = new ConcurrentHashMap<>();

    public void validate(MethodParameter parameter, Object value, Class<?>[] groups) throws MethodArgumentNotValidException {
        Class<?> actualType = value.getClass();
        String objectName = nameOr(actualType.getSimpleName(), parameter.getParameterName());
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(value, objectName);

        // 1) Bean Validation
        if (groups != null && groups.length > 0) {
            smartValidator.validate(value, errors, groups);
        } else {
            smartValidator.validate(value, errors);
        }

        // 2) 业务校验
        List<AxFlowValidator<?>> validators = cache.computeIfAbsent(actualType, this::loadValidators);
        for (AxFlowValidator<?> v : validators) {
            @SuppressWarnings("unchecked")
            AxFlowValidator<Object> v0 = (AxFlowValidator<Object>) v;
            AxFlowValidationResult vr = v0.validate(value);
            if (!vr.isValid()) {
                vr.getErrors().forEach(msg -> errors.addError(new ObjectError(objectName, msg)));
            }
        }

        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(parameter, errors);
        }
    }

    @SuppressWarnings("rawtypes")
    private List<AxFlowValidator<?>> loadValidators(Class<?> actualType) {
        // Spring 返回的是原生类型集合：Collection<AxFlowValidator>
        Collection<AxFlowValidator> beans =
                applicationContext.getBeansOfType(AxFlowValidator.class).values();

        // 逐个安全转成 ? 形态
        List<AxFlowValidator<?>> list = new ArrayList<>(beans.size());
        for (AxFlowValidator v : beans) {
            list.add(v);
        }

        // 过滤 supports，排序
        list.removeIf(v -> !safeSupports(v, actualType));
        list.sort(Comparator.comparingInt(this::orderOf));
        return List.copyOf(list);
    }

    private boolean safeSupports(AxFlowValidator<?> v, Class<?> t) {
        try {
            return v.supports(t);
        } catch (Exception e) {
            return false;
        }
    }

    private int orderOf(AxFlowValidator<?> v) {
        Order o = v.getClass().getAnnotation(Order.class);
        return o != null ? o.value() : Integer.MAX_VALUE;
    }

    private String nameOr(String fallback, @Nullable String n) {
        return (n == null || n.isBlank()) ? fallback : n;
    }
}
