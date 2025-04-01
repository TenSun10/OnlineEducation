package com.tenxi.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxi.utils.JwtUtils;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.Account;
import com.tenxi.entity.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.tenxi.utils.ConstStr.ACCOUNT_LOGIN;

@Log
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LoginUser user = (LoginUser)authentication.getPrincipal();
        Account account = user.getAccount();

        log.info(account.getEmail() + "用户登录成功");

        //把用户的信息存储到redis中
        String asString = new ObjectMapper().writeValueAsString(account);
        stringRedisTemplate.opsForValue().set(ACCOUNT_LOGIN + account.getId(), asString, 2, TimeUnit.DAYS);

        //生成jwt返回
        Map<String, String> data = new HashMap<>();
        data.put("user_id", account.getId().toString());

        String jwt = JwtUtils.generateJwt(data);

        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(JSON.toJSONString(RestBean.successWithData(jwt)));

    }
}
