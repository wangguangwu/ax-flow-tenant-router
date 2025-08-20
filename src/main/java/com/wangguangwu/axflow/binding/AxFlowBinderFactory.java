package com.wangguangwu.axflow.binding;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 绑定工厂：选择 Binder → 决定目标类型 → 反序列化 → afterBind。
 *
 * @author wangguangwu
 */
@Component
public class AxFlowBinderFactory {

    private final ApplicationContext applicationContext;
    private final MappingJackson2HttpMessageConverter jackson;

    public AxFlowBinderFactory(ApplicationContext applicationContext, MappingJackson2HttpMessageConverter jackson) {
        this.applicationContext = applicationContext;
        this.jackson = jackson;
    }

    public Bound bind(byte[] rawBody, Class<?> baseType) {
        AxFlowBinder binder = resolveBinder(baseType);
        AxFlowBinder.Target target;
        try {
            target = binder.resolveTarget(rawBody, baseType);
        } catch (Exception e) {
            throw new IllegalArgumentException("决定目标类型失败: baseType=%s, error=%s"
                    .formatted(baseType.getSimpleName(), e.getMessage()), e);
        }

        ObjectMapper mapper = jackson.getObjectMapper();
        final Object value;
        try {
            value = mapper.readValue(rawBody, target.type());
        } catch (Exception e) {
            String raw = new String(rawBody, StandardCharsets.UTF_8);
            throw new IllegalArgumentException("请求体反序列化失败: targetType=%s, error=%s, raw=%s"
                    .formatted(target.type().getSimpleName(), e.getMessage(), raw), e);
        }

        try {
            binder.afterBind(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("绑定后处理失败: targetType=%s, error=%s"
                    .formatted(target.type().getSimpleName(), e.getMessage()), e);
        }

        return new Bound(value);
    }

    public record Bound(Object value) {
    }

    private AxFlowBinder resolveBinder(Class<?> baseType) {
        Collection<AxFlowBinder> all = applicationContext.getBeansOfType(AxFlowBinder.class).values();
        List<AxFlowBinder> candidates = all.stream()
                .filter(b -> !(b instanceof com.wangguangwu.axflow.binding.AxFlowNoopBinder))
                .filter(b -> safeSupport(b, baseType))
                .sorted(Comparator.comparingInt(this::orderOf))
                .toList();

        if (!candidates.isEmpty()) {
            int top = orderOf(candidates.get(0));
            if (candidates.size() > 1 && orderOf(candidates.get(1)) == top) {
                throw new IllegalStateException("存在多个相同优先级的 Binder: baseType=" + baseType.getSimpleName());
            }
            return candidates.get(0);
        }
        return applicationContext.getBean(com.wangguangwu.axflow.binding.AxFlowNoopBinder.class);
    }

    private boolean safeSupport(AxFlowBinder b, Class<?> baseType) {
        try {
            return b.supportsBaseType(baseType);
        } catch (Exception e) {
            return false;
        }
    }

    private int orderOf(AxFlowBinder b) {
        var ann = b.getClass().getAnnotation(org.springframework.core.annotation.Order.class);
        if (ann != null) {
            return ann.value();
        }
        if (b instanceof org.springframework.core.Ordered o) {
            return o.getOrder();
        }
        return Integer.MAX_VALUE;
    }
}
