package com.wangguangwu.axflowtenantrouter.config;

import com.wangguangwu.axflowtenantrouter.core.resolver.SimpleTenantBodyArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 简化的租户配置类
 * <p>
 * 负责注册简化版的多租户请求体参数解析器
 *
 * @author wangguangwu
 */
@Configuration
@RequiredArgsConstructor
public class SimpleTenantConfig implements WebMvcConfigurer {

    /**
     * 简化的多租户请求体参数解析器
     */
    private final SimpleTenantBodyArgumentResolver simpleTenantBodyArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 添加到解析器列表的开头，确保优先使用此解析器
        resolvers.add(0, simpleTenantBodyArgumentResolver);
    }
}
