package com.wangguangwu.axflow.web;

import com.wangguangwu.axflow.annotation.AxFlow;
import com.wangguangwu.axflow.binding.AxFlowBinderFactory;
import com.wangguangwu.axflow.context.TenantContext;
import com.wangguangwu.axflow.validation.AxFlowValidationService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 参数解析器：按方法级 {@link AxFlow} 选择目标参数，完成“读取Body→租户路由绑定→（可选）统一校验”。
 *
 * <p>选择顺序：paramIndex > paramName > 自动推断唯一复杂对象。</p>
 *
 * <p>校验：方法级 validate 优先；否则使用绑定结果的 validateByDefault。</p>
 *
 * @author wangguangwu
 */
@Component
public class AxFlowArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private AxFlowBinderFactory binderFactory;
    @Resource
    private AxFlowValidationService validationService;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        AxFlow ax = getMethodAxFlow(parameter);
        return ax != null && isSelectedParameter(parameter, ax);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            org.springframework.web.bind.support.WebDataBinderFactory ignored) throws Exception {

        HttpServletRequest req = Objects.requireNonNull(
                webRequest.getNativeRequest(HttpServletRequest.class), "请求对象不能为空");
        ServletServerHttpRequest input = new ServletServerHttpRequest(req);

        AxFlow ax = Objects.requireNonNull(getMethodAxFlow(parameter), "@AxFlow 必须标注在方法上");

        byte[] body = StreamUtils.copyToByteArray(req.getInputStream());
        if (body.length == 0 && ax.bodyRequired()) {
            throw new HttpMessageNotReadableException("请求体为空", input);
        }

        // 解析前调用
        checkTenantAccess(ax, TenantContext.getTenantId());

        Class<?> baseType = parameter.getParameterType();
        var bound = binderFactory.bind(body, baseType);
        Object value = bound.value();

        if (ax.validate()) {
            validationService.validate(parameter, value, ax.groups());
        }
        return value;
    }

    private @Nullable AxFlow getMethodAxFlow(MethodParameter parameter) {
        return Optional.ofNullable(parameter.getMethod())
                .map(m -> AnnotatedElementUtils.findMergedAnnotation(m, AxFlow.class))
                .orElse(null);
    }

    private boolean isSelectedParameter(MethodParameter p, AxFlow ax) {
        if (ax.paramIndex() >= 0) {
            return p.getParameterIndex() == ax.paramIndex();
        }
        if (!ax.paramName().isBlank()) {
            String name = p.getParameterName();
            return name != null && name.equals(ax.paramName());
        }
        return isTheOnlyComplexParam(p);
    }

    private boolean isTheOnlyComplexParam(MethodParameter target) {
        var method = Objects.requireNonNull(target.getMethod());
        List<Integer> complexIndexes = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            MethodParameter mp = new MethodParameter(method, i);
            if (isComplex(mp)) {
                complexIndexes.add(i);
            }
        }
        return complexIndexes.size() == 1 && complexIndexes.get(0) == target.getParameterIndex();
    }

    private boolean isComplex(MethodParameter p) {
        Class<?> t = p.getParameterType();
        if (isSimpleValueType(t)) {
            return false;
        }
        if (ServletRequest.class.isAssignableFrom(t) || HttpServletResponse.class.isAssignableFrom(t)) {
            return false;
        }
        for (Annotation a : p.getParameterAnnotations()) {
            if (a.annotationType() == RequestParam.class ||
                    a.annotationType() == PathVariable.class ||
                    a.annotationType() == RequestHeader.class) {
                return false;
            }
        }
        return true;
    }

    private boolean isSimpleValueType(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz)
                || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || java.util.Date.class.isAssignableFrom(clazz)
                || clazz.isEnum();
    }

    /**
     * 校验当前租户是否符合 @AxFlow 的白名单/黑名单规则。
     *
     * @param ax       方法上的 @AxFlow 注解
     * @param tenantId 当前租户 ID，可为空
     */
    private void checkTenantAccess(AxFlow ax, String tenantId) {
        String t = tenantId == null ? "" : tenantId.trim();

        // 如果黑白名单都为空，默认拒绝
        if (ax.allowedTenants().length == 0 && ax.deniedTenants().length == 0) {
            throw new IllegalArgumentException("no tenant access config, reject by default: " + (t.isEmpty() ? "<empty>" : t));
        }

        // 1) 黑名单优先
        if (inList(ax.deniedTenants(), t)) {
            throw new IllegalArgumentException("tenant denied: " + (t.isEmpty() ? "<empty>" : t));
        }

        // 2) 白名单：若配置了白名单且未命中，则拒绝
        if (ax.allowedTenants().length > 0 && !inList(ax.allowedTenants(), t)) {
            throw new IllegalArgumentException("tenant not allowed: " + (t.isEmpty() ? "<empty>" : t));
        }
    }

    /**
     * 判断 tenant 是否在名单中，支持通配符 "*"。
     */
    private boolean inList(String[] configured, String tenantId) {
        if (configured == null) {
            return false;
        }
        for (String c : configured) {
            if (c == null || c.isBlank()) {
                continue;
            }
            if ("*".equals(c.trim()) || c.trim().equals(tenantId)) {
                return true;
            }
        }
        return false;
    }
}
