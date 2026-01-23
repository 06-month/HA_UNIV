package com.university.grade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로덕션 환경에서는 JAR 내부의 static 리소스 사용
        if ("prod".equals(activeProfile)) {
            registry.addResourceHandler("/login/**")
                    .addResourceLocations("classpath:/static/login/");
            
            registry.addResourceHandler("/main/**")
                    .addResourceLocations("classpath:/static/main/");
        } else {
            // 개발 환경에서는 외부 파일 시스템 사용
            String frontendPath = Paths.get("..", "frontend", "src").toAbsolutePath().normalize().toString();
            
            // 경로가 존재하는지 확인
            if (Files.exists(Paths.get(frontendPath))) {
                registry.addResourceHandler("/login/**")
                        .addResourceLocations("file:" + frontendPath + "/login/");
                
                registry.addResourceHandler("/main/**")
                        .addResourceLocations("file:" + frontendPath + "/main/");
            } else {
                // 경로가 없으면 classpath 사용 (fallback)
                registry.addResourceHandler("/login/**")
                        .addResourceLocations("classpath:/static/login/");
                
                registry.addResourceHandler("/main/**")
                        .addResourceLocations("classpath:/static/main/");
            }
        }
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로는 login으로 리다이렉트
        registry.addRedirectViewController("/", "/login/index.html");
    }
}
