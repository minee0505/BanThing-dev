package com.nathing.banthing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-url}")
    private String uploadUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /media/** URL 패턴으로 요청이 오면
        registry.addResourceHandler(uploadUrl + "**")
                // 로컬 디스크의 C:/banthing_uploads/ 경로에서 파일을 찾아 제공합니다.
                // "file:///" 접두사는 필수입니다.
                .addResourceLocations("file:" + uploadDir);
    }
}