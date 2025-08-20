package com.wangguangwu.axflow.registry;

import com.wangguangwu.axflow.annotation.AxFlowModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 扫描 @AxFlowModel 并建立 (baseType, tenantId) -> SubtypeMeta 的只读映射表。
 * 线程安全要点：
 * 1) 构建阶段使用局部可变结构（不对外暴露）
 * 2) 构建完成后深度“冻结”为不可变 Map
 * 3) 通过 AtomicReference 原子发布快照，读者始终读取一致视图
 * <p>
 * baseType 计算规则（可选显式、默认智能）：
 * - 若注解 base() 非 Void.class：使用注解指定值，并校验子类关系；
 * - 否则：沿 superclass 向上取“最顶层非 Object 父类”；若无非 Object 父类，则使用当前类自身。
 *
 * @author wangguangwu
 */
@Component
public class AxFlowSubtypeRegistry {

    /**
     * 子类元信息：仅记录子类类型
     */
    public record SubtypeMeta(Class<?> subtype) {
    }

    /**
     * 配置的扫描包（逗号分隔）
     */
    private final List<String> scanBasePackages;

    /**
     * 原子保存“当前生效”的不可变路由表快照
     */
    private final AtomicReference<Map<Class<?>, Map<String, SubtypeMeta>>> tableRef =
            new AtomicReference<>(Map.of());

    public AxFlowSubtypeRegistry(@Value("${axflow.scan-base-packages:}") String scanPkgs) {
        if (StringUtils.hasText(scanPkgs)) {
            this.scanBasePackages = Arrays.stream(scanPkgs.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();
        } else {
            // 默认示例包路径（按需修改）
            this.scanBasePackages = List.of("com.wangguangwu.axflow.sample.model");
        }
    }

    /**
     * 容器启动后执行一次：扫描 → 构建路由表 → 冻结 → 原子发布
     */
    @PostConstruct
    public void init() {
        Map<Class<?>, Map<String, SubtypeMeta>> built = buildMutableTable(scanBasePackages);

        // 深度不可变化（两层 Map 都不可变）
        Map<Class<?>, Map<String, SubtypeMeta>> frozen = built.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Map.copyOf(e.getValue())
                ));

        // 原子发布快照
        tableRef.set(frozen);
    }

    /**
     * 解析：先用 baseType 找到该 base 的租户映射，再取 tenantId 对应的子类元信息
     */
    public Optional<SubtypeMeta> resolve(Class<?> baseType, String tenantId) {
        Map<Class<?>, Map<String, SubtypeMeta>> snapshot = tableRef.get();
        Map<String, SubtypeMeta> m = snapshot.get(baseType);
        if (m == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(m.get(tenantId));
    }

    // ===================== 内部：构建路由表（可变阶段，仅在 init 使用） =====================

    /**
     * 扫描并构建 (baseType, tenantId) -> SubtypeMeta 的可变表
     */
    private Map<Class<?>, Map<String, SubtypeMeta>> buildMutableTable(List<String> basePkgs) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AxFlowModel.class));

        ClassLoader cl = ClassUtils.getDefaultClassLoader();
        Map<Class<?>, Map<String, SubtypeMeta>> table = new LinkedHashMap<>();

        for (String basePkg : basePkgs) {
            var candidates = scanner.findCandidateComponents(basePkg);
            for (var bd : candidates) {
                // 1) 加载类（不可加载则跳过）
                Optional<Class<?>> clazzOpt = loadClass(bd.getBeanClassName(), cl);
                if (clazzOpt.isEmpty()) {
                    continue;
                }
                Class<?> clazz = clazzOpt.get();

                // 2) 提取注解（没有注解则跳过）
                Optional<AxFlowModel> annOpt = getAxFlowModel(clazz);
                if (annOpt.isEmpty()) {
                    continue;
                }
                AxFlowModel ann = annOpt.get();

                // 3) 计算 baseType（可选显式、默认智能推导）
                Class<?> baseType = deriveBaseType(clazz, ann);

                // 4) 规整租户列表
                List<String> tenants = normalizeTenants(ann, clazz);

                // 5) 组装元信息并合并入表（冲突检测）
                SubtypeMeta meta = new SubtypeMeta(clazz);
                mergeMapping(table, baseType, tenants, meta);
            }
        }
        return table;
    }

    // ================== 私有辅助方法（职责单一，便于单测） ==================

    /**
     * 安全加载类：加载失败返回 Optional.empty()
     */
    private Optional<Class<?>> loadClass(String fqcn, ClassLoader cl) {
        try {
            return Optional.of(ClassUtils.forName(fqcn, cl));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    /**
     * 读取类上的 @AxFlowModel 注解，没有则返回 Optional.empty()
     */
    private Optional<AxFlowModel> getAxFlowModel(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(AxFlowModel.class));
    }

    /**
     * 计算 baseType：
     * 1) 若注解 base() 非 Void.class：校验并使用注解提供的 base；
     * 2) 否则，沿 superclass 链向上寻找“最顶层非 Object 父类”；
     * 3) 若不存在非 Object 父类，则使用 clazz 自身。
     */
    private Class<?> deriveBaseType(Class<?> clazz, AxFlowModel ann) {
        Class<?> annoBase = ann.base();
        if (annoBase != null && annoBase != Void.class) {
            if (!annoBase.isAssignableFrom(clazz)) {
                throw new IllegalStateException(
                        "@AxFlowModel 声明非法：子类 %s 不是 base %s 的子类型"
                                .formatted(clazz.getName(), annoBase.getName()));
            }
            return annoBase;
        }
        // 未显式指定：向上推导
        Class<?> cur = clazz.getSuperclass();
        Class<?> last = null;
        while (cur != null && cur != Object.class) {
            last = cur;
            cur = cur.getSuperclass();
        }
        return (last != null) ? last : clazz;
    }

    /**
     * 规整租户列表：去空白、去重、非空校验
     */
    private List<String> normalizeTenants(AxFlowModel ann, Class<?> clazz) {
        List<String> tenants = Arrays.stream(ann.value())
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
        if (tenants.isEmpty()) {
            throw new IllegalStateException("@AxFlowModel 缺少租户列表：" + clazz.getName());
        }
        return tenants;
    }

    /**
     * 合并到路由表；对同一 (base, tenant) 的不同子类抛出冲突异常
     */
    private void mergeMapping(Map<Class<?>, Map<String, SubtypeMeta>> table,
                              Class<?> baseType,
                              List<String> tenants,
                              SubtypeMeta meta) {
        Map<String, SubtypeMeta> inner = table.computeIfAbsent(baseType, k -> new LinkedHashMap<>());
        for (String tenantId : tenants) {
            SubtypeMeta prev = inner.putIfAbsent(tenantId, meta);
            if (prev != null && !prev.subtype().equals(meta.subtype())) {
                throw new IllegalStateException(
                        "重复映射: base=%s, tenant=%s, exist=%s, new=%s"
                                .formatted(
                                        baseType.getName(),
                                        tenantId,
                                        prev.subtype().getName(),
                                        meta.subtype().getName()
                                )
                );
            }
        }
    }
}