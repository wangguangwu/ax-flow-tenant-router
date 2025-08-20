package com.wangguangwu.axflow.config;

import com.wangguangwu.axflow.web.AxFlowArgumentResolver;
import com.wangguangwu.axflow.web.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC 配置：注册 {@link AxFlowArgumentResolver}。
 *
 * @author wangguangwu
 */
@Configuration
public class AxFlowWebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final AxFlowArgumentResolver axFlowArgumentResolver;

    public AxFlowWebConfig(TenantInterceptor tenantInterceptor, AxFlowArgumentResolver axFlowArgumentResolver) {
        this.tenantInterceptor = tenantInterceptor;
        this.axFlowArgumentResolver = axFlowArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, axFlowArgumentResolver);
    }
}
