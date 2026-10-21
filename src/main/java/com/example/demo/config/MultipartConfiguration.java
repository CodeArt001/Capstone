package com.example.demo.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipartConfiguration {
    private static final long MAX_UPLOAD_SIZE = 10L * 1024 * 1024;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("", MAX_UPLOAD_SIZE, MAX_UPLOAD_SIZE, 0);
    }
}