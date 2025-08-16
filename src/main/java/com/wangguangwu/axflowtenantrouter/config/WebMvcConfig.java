package com.wangguangwu.axflowtenantrouter.config;

import com.wangguangwu.axflowtenantrouter.core.resolver.TenantBodyArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC配置类
 * <p>
 * 负责注册自定义参数解析器，使多租户请求体参数解析器生效
 *
 * @author wangguangwu
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 多租户请求体参数解析器
     */
    private final TenantBodyArgumentResolver tenantBodyArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 添加到解析器列表的开头，确保优先使用此解析器
        resolvers.add(0, tenantBodyArgumentResolver);
    }
}
