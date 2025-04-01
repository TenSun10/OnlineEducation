package com.tenxi.interceptor;

import com.tenxi.entity.LoginUser;
import com.tenxi.exception.JwtException;
import com.tenxi.utils.BaseContext;
import feign.RequestTemplate;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static com.tenxi.utils.ConstStr.ACCOUNT_LOGIN;

/**
 * 用于处理放行路径下
 * 用户身份信息在redis的缓存刷新
 */

@Component
@ConditionalOnClass(DispatcherServlet.class)
public class RefreshAuthenticationInterceptor implements HandlerInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LoginUser user = (LoginUser)SecurityContextHolder.getContext().getAuthentication();
        Long userId = user.getAccount().getId();

        String redisKey = ACCOUNT_LOGIN + userId;
        String userJson = stringRedisTemplate.opsForValue().get(redisKey);

        //刷新用户身份信息的缓存时间
        if (userJson != null) {
            stringRedisTemplate.opsForValue().set(redisKey, userJson, 2, TimeUnit.DAYS);
        }
        return true;
    }
}
