package com.wangguangwu.axflowtenantrouter.core.registry;

import com.wangguangwu.axflowtenantrouter.annotation.TenantRoute;
import com.wangguangwu.axflowtenantrouter.model.common.Holder;
import com.wangguangwu.axflowtenantrouter.model.common.RouteKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 抽象租户注册表基类
 * <p>
 * 扫描带 @TenantRoute 的 Bean，建立 (RouteKey -> Holder[]) 路由表
 *
 * @param <T> 注册的Bean类型
 * @author wangguangwu
 */
@RequiredArgsConstructor
public abstract class AbstractTenantRegistry<T> implements ApplicationListener<ContextRefreshedEvent> {

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
        registerBeans(getBeanType(), routeTable);
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
     * 从Bean实例中提取目标类型
     *
     * @param bean Bean实例
     * @return 目标类型的Class对象
     */
    protected abstract Class<?> extractTargetType(Object bean);

    /**
     * 获取注册表名称（用于日志和错误信息）
     *
     * @return 注册表名称
     */
    protected abstract String getRegistryName();

    /**
     * 扫描并注册带有@TenantRoute注解的Bean
     *
     * @param type     Bean类型
     * @param registry 注册表
     * @param <X>      Bean类型参数
     */
    private <X> void registerBeans(Class<X> type, Map<RouteKey, List<Holder>> registry) {
        applicationContext.getBeansOfType(type)
                .values()
                .stream()
                .map(bean -> Map.entry(bean, bean.getClass().getAnnotation(TenantRoute.class)))
                .filter(e -> e.getValue() != null)
                .forEach(e -> expandKeys(e.getValue().controller(), e.getValue().methods())
                        .forEach(key -> {
                            RouteKey routeKey = new RouteKey(e.getValue().tenant(), key, e.getValue().base());
                            registry.computeIfAbsent(routeKey, ignored -> new ArrayList<>())
                                    .add(new Holder(extractTargetType(e.getKey()), e.getKey(), e.getValue().order()));
                        }));
    }

    /**
     * 展开控制器和方法名为完全限定名键
     *
     * @param controller 控制器类
     * @param methods    方法名数组
     * @return 展开后的键集合
     */
    protected Set<String> expandKeys(Class<?> controller, String[] methods) {
        // 如果是通配所有控制器和方法
        if (controller == Object.class && Arrays.equals(methods, new String[]{WILDCARD})) {
            return Set.of(WILDCARD);
        }
        
        // 获取控制器中所有方法名
        Set<String> allMethodNames = Arrays.stream(controller.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
        
        // 展开方法名（支持通配符）
        return Arrays.stream(methods)
                .flatMap(method -> WILDCARD.equals(method) 
                        ? allMethodNames.stream() 
                        : Stream.of(validateMethod(controller, allMethodNames, method)))
                .map(method -> controller.getName() + "#" + method)
                .collect(Collectors.toSet());
    }

    /**
     * 验证方法是否存在于控制器中
     *
     * @param controller    控制器类
     * @param allMethods    控制器中所有方法名
     * @param methodToCheck 待检查的方法名
     * @return 验证通过的方法名
     */
    private String validateMethod(Class<?> controller, Set<String> allMethods, String methodToCheck) {
        if (!allMethods.contains(methodToCheck)) {
            throw new IllegalStateException("Method not found: " + controller.getName() + "#" + methodToCheck);
        }
        return methodToCheck;
    }

    /**
     * 对注册表中的项进行排序并检查冲突
     *
     * @param table 注册表
     * @param kind  注册表类型名称
     */
    private void sortAndCheck(Map<RouteKey, List<Holder>> table, String kind) {
        // 按优先级排序
        table.values().forEach(list -> list.sort(Comparator.comparingInt(Holder::getOrder)));
        
        // 检查冲突（同一路由键下有多个相同优先级的项）
        table.entrySet().stream()
                .filter(e -> e.getValue().size() > 1
                        && e.getValue().get(0).getOrder() == e.getValue().get(1).getOrder())
                .findAny()
                .ifPresent(e -> {
                    throw new IllegalStateException(kind + " conflict on " + e.getKey()
                            + ", same order for multiple beans");
                });
    }

    /**
     * 查找匹配的注册项
     * <p>
     * 默认匹配顺序：
     * 1. 精确匹配（租户精确，方法精确）
     * 2. 方法精确匹配（租户通配，方法精确）
     * 3. 租户精确匹配（租户精确，方法通配）
     * 4. 完全通配（租户通配，方法通配）
     *
     * @param tenant          租户ID
     * @param key             路由键（类名#方法名）
     * @param base            基类类型
     * @param targetTypeFilter 目标类型过滤器（可选）
     * @return 匹配的注册项
     */
    protected Optional<Holder> find(@NonNull String tenant, @NonNull String key, @NonNull Class<?> base,
                                    @Nullable Class<?> targetTypeFilter) {
        return Stream.of(
                        new RouteKey(tenant, key, base),
                        new RouteKey(WILDCARD, key, base),
                        new RouteKey(tenant, WILDCARD, base),
                        new RouteKey(WILDCARD, WILDCARD, base)
                )
                .map(routeTable::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                // 基类类型匹配
                .filter(h -> base.isAssignableFrom(h.getTargetType()))
                // 目标类型过滤（如果提供）
                .filter(h -> targetTypeFilter == null || h.getTargetType().isAssignableFrom(targetTypeFilter))
                .findFirst();
    }
}
