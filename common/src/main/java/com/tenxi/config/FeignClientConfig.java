package com.tenxi.config;

import com.tenxi.interceptor.FeignClientAuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    @Bean
    public FeignClientAuthInterceptor feignClientAuthInterceptor() {
        return new FeignClientAuthInterceptor();
    }
}
