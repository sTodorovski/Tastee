package com.example.tasteebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDir = new File("uploads");
        String uploadPath = uploadDir.getAbsolutePath();

        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + File.separator);
    }
}