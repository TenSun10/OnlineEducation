package com.tenxi.progress.config;

import com.tenxi.filter.HeaderAuthFilter;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Resource
    private HeaderAuthFilter headerAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                         authorize.anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable) //显示禁用表单登录
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                //异常处理 - 与业务逻辑异常构成全局异常处理
                .exceptionHandling(handler -> {
                    handler.authenticationEntryPoint((req, res, ex) -> {
                        res.setContentType("application/json;charset=utf-8");
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.getWriter().write(RestBean.unauthorized("未登录").asJsonString());
                    });
                    handler.accessDeniedHandler((req, res, ex) -> {
                        res.setContentType("application/json;charset=utf-8");
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.getWriter().write(RestBean.forbidden("无权限").asJsonString());
                    });
                });

        return http.build();
    }
}
