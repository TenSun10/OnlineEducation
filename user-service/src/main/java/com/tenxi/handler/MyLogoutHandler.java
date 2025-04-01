package com.tenxi.handler;

import com.tenxi.entity.Account;
import com.tenxi.entity.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.tenxi.utils.ConstStr.ACCOUNT_LOGIN;

@Log
@Component
public class MyLogoutHandler implements LogoutSuccessHandler {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //取出用户的id,在redis中删除用户的信息
        LoginUser user = (LoginUser)authentication.getPrincipal();
        Account account = user.getAccount();

        log.info(account.getEmail() + "用户登出成功");

        stringRedisTemplate.delete(ACCOUNT_LOGIN + account.getId());
    }
}
