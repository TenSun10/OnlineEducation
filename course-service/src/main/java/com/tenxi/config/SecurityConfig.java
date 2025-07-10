package com.tenxi.config;

import com.tenxi.utils.RestBean;
import com.tenxi.filter.HeaderAuthFilter;
import com.tenxi.service.impl.LoginServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration("courseSecurityConfig")
//允许调用方法之前的权限验证
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Resource
    private HeaderAuthFilter headerAuthFilter;

    @Resource
    private LoginServiceImpl loginService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 1.经过headerAuthFilter
     * 2.判断是否需要经过认证
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/category/tree").permitAll()
                        .anyRequest().authenticated()
                )
                // 显式禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)  // 关键修改
                .rememberMe(rememberMe ->
                        rememberMe.userDetailsService(loginService))
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                //注意:spring security的异常处理优先级高于业务逻辑异常处理 - 指的是安全过滤器链中的异常处理机制会在请求进入Controller层之前拦截认证/授权异常
                //所以当出现业务逻辑异常的时候也会报错401或者403
                //那么我们就需要定义自己的全局异常处理器
                .exceptionHandling(handler -> {
                    handler.authenticationEntryPoint((req, res, ex) -> {
                        // 统一返回 JSON 格式的 401 响应
                        res.setContentType("application/json;charset=utf-8");
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.getWriter().write(RestBean.unauthorized("未登录").asJsonString());
                    });
                    handler.accessDeniedHandler((req, res, ex) -> {
                        // 统一返回 JSON 格式的 403 响应
                        res.setContentType("application/json;charset=utf-8");
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.getWriter().write(RestBean.forbidden("无权限").asJsonString());
                    });
                });

        return http.build();
    }
}
