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
        // 1. JAR 내부의 static 디렉토리 (클라우드 배포 시)
        registry.addResourceHandler("/login/**")
                .addResourceLocations("classpath:/static/login/");
        
        registry.addResourceHandler("/main/**")
                .addResourceLocations("classpath:/static/main/");
        
        // 2. 개발 환경: 외부 경로도 지원 (로컬 개발 시)
        try {
            String frontendPath = Paths.get("..", "frontend", "src").toAbsolutePath().normalize().toString();
            java.io.File frontendDir = new java.io.File(frontendPath);
            if (frontendDir.exists()) {
                registry.addResourceHandler("/login/**")
                        .addResourceLocations("file:" + frontendPath + "/login/")
                        .resourceChain(false);
                
                registry.addResourceHandler("/main/**")
                        .addResourceLocations("file:" + frontendPath + "/main/")
                        .resourceChain(false);
            }
        } catch (Exception e) {
            // 외부 경로가 없으면 JAR 내부 리소스만 사용
        }
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로는 login으로 리다이렉트
        registry.addRedirectViewController("/", "/login/index.html");
    }
}
