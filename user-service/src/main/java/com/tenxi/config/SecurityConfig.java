package com.tenxi.config;

import com.tenxi.filter.HeaderAuthFilter;
import com.tenxi.filter.JsonTransFilter;
import com.tenxi.handler.MyAuthenticationFailureHandler;
import com.tenxi.handler.MyAuthenticationSuccessHandler;
import com.tenxi.handler.MyLogoutHandler;
import com.tenxi.service.impl.LoginServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Resource
    private LoginServiceImpl loginService;

    @Resource
    private MyLogoutHandler myLogoutHandler;

    @Resource
    private HeaderAuthFilter headerAuthFilter;


    @Resource
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;

    @Resource
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users/login").anonymous()
                        .requestMatchers("/users/register", "/users/ask-code").permitAll()
                        .anyRequest().authenticated()
                )
                //用于指定我的实现
                .rememberMe(rememberMe -> rememberMe
                        .userDetailsService(loginService))
                //记住我功能,用于网页关闭之后浏览器依旧保持登录状态
                .rememberMe(Customizer.withDefaults())
                .logout(config -> config
                        .logoutUrl("/users/logout")
                        .logoutSuccessHandler(myLogoutHandler))
                .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jsonTransFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable);


        return http.build();
    }

    @Bean
    public JsonTransFilter jsonTransFilter(AuthenticationManager authenticationManager) {
        JsonTransFilter jsonTransFilter = new JsonTransFilter();
        jsonTransFilter.setAuthenticationManager(authenticationManager);
        jsonTransFilter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler);
        jsonTransFilter.setAuthenticationFailureHandler(myAuthenticationFailureHandler);
        return jsonTransFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
