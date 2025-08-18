package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.model.common.RouteKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

/**
 * 简化的租户注册表基类
 * <p>
 * 扫描带有指定注解的 Bean，建立 (RouteKey -> Holder[]) 路由表
 * 通过泛型自动推断目标类型，无需手动指定控制器和方法
 *
 * @param <T> 注册的Bean类型
 * @author wangguangwu
 */
@RequiredArgsConstructor
public abstract class TenantRegistry<T> implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * 通配符常量
     */
    protected static final String WILDCARD = "*";

    /**
     * Spring应用上下文
     */
    protected final ApplicationContext applicationContext;
    
    /**
     * 路由表：RouteKey -> Holder列表
     */
    protected final Map<RouteKey, List<Holder>> routeTable = new HashMap<>();

    @Override
    public final void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // 1) 扫描并注册
        registerBeans(getBeanType(), getAnnotationType(), routeTable);
        // 2) 排序 & 冲突检测
        sortAndCheck(routeTable, getRegistryName());
    }

    /**
     * 获取要注册的Bean类型
     *
     * @return Bean类型的Class对象
     */
    protected abstract Class<T> getBeanType();

    /**
     * 获取注解类型
     *
     * @return 注解类型的Class对象
     */
    protected abstract Class<? extends Annotation> getAnnotationType();

    /**
     * 从Bean实例中提取目标类型
     *
     * @param bean Bean实例
     * @return 目标类型的Class对象
     */
    protected abstract Class<?> extractTargetType(Object bean);

    /**
     * 从注解中提取租户ID
     *
     * @param annotation 注解实例
     * @return 租户ID
     */
    protected abstract String extractTenant(Annotation annotation);

    /**
     * 从注解中提取优先级
     *
     * @param annotation 注解实例
     * @return 优先级
     */
    protected abstract int extractOrder(Annotation annotation);

    /**
     * 获取注册表名称（用于日志和错误信息）
     *
     * @return 注册表名称
     */
    protected abstract String getRegistryName();

    /**
     * 扫描并注册带有指定注解的Bean
     *
     * @param type         Bean类型
     * @param annotationType 注解类型
     * @param registry     注册表
     * @param <X>          Bean类型参数
     */
    private <X> void registerBeans(Class<X> type, Class<? extends Annotation> annotationType, Map<RouteKey, List<Holder>> registry) {
        applicationContext.getBeansOfType(type)
                .values()
                .stream()
                .map(bean -> Map.entry(bean, bean.getClass().getAnnotation(annotationType)))
                .filter(e -> e.getValue() != null)
                .forEach(e -> {
                    // 从Bean中提取目标类型
                    Class<?> targetType = extractTargetType(e.getKey());
                    // 从目标类型推断基类
                    Class<?> baseType = inferBaseType(targetType);
                    // 从注解中提取租户ID和优先级
                    String tenant = extractTenant(e.getValue());
                    int order = extractOrder(e.getValue());
                    
                    // 创建通配路由键（简化设计，不再指定具体控制器和方法）
                    RouteKey routeKey = new RouteKey(tenant, WILDCARD, baseType);
                    registry.computeIfAbsent(routeKey, ignored -> new ArrayList<>())
                            .add(new Holder(targetType, e.getKey(), order));
                });
    }

    /**
     * 从目标类型推断基类
     * 
     * @param targetType 目标类型
     * @return 基类类型
     */
    protected Class<?> inferBaseType(Class<?> targetType) {
        // 默认实现：返回直接父类
        // 子类可以覆盖此方法以提供更复杂的基类推断逻辑
        return targetType.getSuperclass();
    }

    /**
     * 对注册表中的项进行排序并检查冲突
     *
     * @param table 注册表
     * @param kind  注册表类型名称
     */
    private void sortAndCheck(Map<RouteKey, List<Holder>> table, String kind) {
        // 按优先级排序
        table.values().forEach(list -> list.sort(Comparator.comparingInt(Holder::order)));
        
        // 检查冲突（同一路由键下有多个相同优先级的项）
        table.entrySet().stream()
                .filter(e -> e.getValue().size() > 1
                        && e.getValue().get(0).order() == e.getValue().get(1).order())
                .findAny()
                .ifPresent(e -> {
                    throw new IllegalStateException(kind + " conflict on " + e.getKey()
                            + ", same order for multiple beans");
                });
    }

    /**
     * 查找匹配的注册项
     *
     * @param tenant          租户ID
     * @param targetType      目标类型
     * @return 匹配的注册项
     */
    protected Optional<Holder> find(@NonNull String tenant, @NonNull Class<?> targetType) {
        // 从目标类型推断基类
        Class<?> baseType = inferBaseType(targetType);
        
        return Stream.of(
                        new RouteKey(tenant, WILDCARD, baseType),
                        new RouteKey(WILDCARD, WILDCARD, baseType)
                )
                .map(routeTable::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                // 目标类型匹配
                .filter(h -> h.targetType().isAssignableFrom(targetType))
                .findFirst();
    }
}
