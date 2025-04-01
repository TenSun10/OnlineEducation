package com.tenxi.handler;

import com.tenxi.utils.RestBean;
import com.tenxi.exception.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@Log
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("用户登录失败");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        if (exception instanceof UsernameNotFoundException) {
            out.write(RestBean.unauthorized("用户不存在").asJsonString());
        }else if (exception instanceof JwtException) {
            out.write(RestBean.unauthorized(exception.getMessage()).asJsonString());
        }
    }
}
