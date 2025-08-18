package com.wangguangwu.axflowtenantrouter.config;

import com.wangguangwu.axflowtenantrouter.core.interceptor.TenantInterceptor;
import com.wangguangwu.axflowtenantrouter.core.resolver.TenantBodyArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WebMvc配置类
 * <p>
 * 配置拦截器、参数解析器等Web相关组件
 *
 * @author wangguangwu
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final TenantBodyArgumentResolver tenantBodyArgumentResolver;

    /**
     * 添加拦截器配置
     * <p>
     * 注意：拦截器会在参数解析器之前执行
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
    }

    /**
     * 添加参数解析器
     * <p>
     * 确保在拦截器之后执行，以便能获取到租户ID
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 添加自定义参数解析器
        resolvers.add(tenantBodyArgumentResolver);
    }
}
