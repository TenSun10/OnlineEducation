package com.tenxi.notification.config;

import com.tenxi.filter.HeaderAuthFilter;
import com.tenxi.notification.service.impl.LoginServiceImpl;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//使用显式命名（防止Spring容器中出现bean名字冲突）
@Configuration("notificationSecurityConfig")
@EnableMethodSecurity(
        prePostEnabled = true
)
public class SecurityConfig {
    @Resource
    private LoginServiceImpl loginService;
    @Resource
    private HeaderAuthFilter headerAuthFilter;

    public SecurityConfig() {
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> {
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)authorize.anyRequest()).authenticated();
        }).formLogin(AbstractHttpConfigurer::disable).rememberMe((rememberMe) -> {
            rememberMe.userDetailsService(this.loginService);
        }).addFilterBefore(this.headerAuthFilter, UsernamePasswordAuthenticationFilter.class).csrf(AbstractHttpConfigurer::disable).exceptionHandling((handler) -> {
            handler.authenticationEntryPoint((req, res, ex) -> {
                res.setContentType("application/json;charset=utf-8");
                res.setStatus(401);
                res.getWriter().write(RestBean.unauthorized("未登录").asJsonString());
            });
            handler.accessDeniedHandler((req, res, ex) -> {
                res.setContentType("application/json;charset=utf-8");
                res.setStatus(403);
                res.getWriter().write(RestBean.forbidden("无权限").asJsonString());
            });
        });
        return (SecurityFilterChain)http.build();
    }
}

