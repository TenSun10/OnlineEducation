package com.tenxi.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.util.Map;


@Log
public class JsonTransFilter extends UsernamePasswordAuthenticationFilter {
    public JsonTransFilter() {
        setFilterProcessesUrl("/users/login");
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if(request.getContentType().equals("application/json")) {
            log.info("进入JsonTransFilter, 准备解析Json数据");
            UsernamePasswordAuthenticationToken authRequest;
            try(ServletInputStream stream = request.getInputStream()) {
                Map map = new ObjectMapper().readValue(stream, Map.class);
                authRequest = new UsernamePasswordAuthenticationToken(map.get("email").toString(), map.get("password").toString());
                setDetails(request, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);
            }
        }
        return super.attemptAuthentication(request, response);
    }
}
