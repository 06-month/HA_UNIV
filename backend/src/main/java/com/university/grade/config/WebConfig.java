package com.university.grade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프론트엔드 정적 리소스 서빙
        String frontendPath = Paths.get("..", "frontend", "src").toAbsolutePath().normalize().toString();
        
        registry.addResourceHandler("/login/**")
                .addResourceLocations("file:" + frontendPath + "/login/");
        
        registry.addResourceHandler("/main/**")
                .addResourceLocations("file:" + frontendPath + "/main/");
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로는 login으로 리다이렉트
        registry.addRedirectViewController("/", "/login/index.html");
    }
}
