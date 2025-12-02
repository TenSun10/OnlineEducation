package com.tenxi.order.config;

import com.tenxi.filter.HeaderAuthFilter;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration("orderSecurityConfig")
public class SecurityConfig {
    @Resource
    private HeaderAuthFilter headerAuthFilter;

    @Bean("orderSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(handler -> {
                    handler.accessDeniedHandler((request, response, ex) -> {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=utf-8");
                        response.getWriter().write(RestBean.forbidden("无权限").asJsonString());
                    });
                    handler.authenticationEntryPoint((request, response, ex) -> {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=utf-8");
                        response.getWriter().write(RestBean.unauthorized("未登录").asJsonString());
                    });
                });

        return http.build();
    }
}
