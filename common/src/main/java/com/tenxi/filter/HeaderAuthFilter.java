package com.tenxi.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxi.enums.ErrorCode;
import com.tenxi.utils.BaseContext;
import com.tenxi.config.AuthProperties;
import com.tenxi.entity.Account;
import com.tenxi.entity.LoginUser;
import com.tenxi.exception.JwtException;
import com.tenxi.exception.PassAuthException;
import com.tenxi.utils.HmacSigner;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tenxi.utils.ConstStr.ACCOUNT_LOGIN;

/**
 * 用于每一个微服务
 * 便于不用重复解析jwt,而是通过gateway中传递过来的头信息
 * X-User-Id从redis中获取用户信息存入SecurityContextHolder
 */

/**
 * 注意：
 * 我们的网关不需要使用Security和这个类，但是网关引入了common
 * 导致就会加载这个类，但是加载又没有Security的依赖导致报错
 * 所以给这个类添加一个Condition
 */

/**
 * 新增traceId的获取
 * 用于日志中添加上下文信息
 */
@Log
@ConditionalOnClass(DispatcherServlet.class)
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        log.info("HeaderAuthFilter拦截到" + uri);
        if(checkPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader("X-User-Id");
        String signature = request.getHeader("X-Signature");
        // 从请求头中获取 traceId
        String traceId = getTraceIdFromRequest(request);


        if (! StringUtils.hasText(userId) || !StringUtils.hasText(signature)) {
            log.info("缺少身份验证头信息");
            throw new PassAuthException(ErrorCode.JWT_NOT_FOUND);
        }

        if(! HmacSigner.verify(userId, signature)) {
            log.info("签名验证失败");
            throw new PassAuthException(ErrorCode.JWT_INVALID_FAILED);
        }

        if (traceId == null) {
            // 如果 traceId 不存在，则生成一个新的
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        String redisKey = ACCOUNT_LOGIN + userId;
        String userJson = stringRedisTemplate.opsForValue().get(redisKey);

        if(!StringUtils.hasText(userJson)) {
            log.info("当前用户的登录信息过期");
            throw new JwtException(ErrorCode.JWT_OUT_OF_DATE);
        }

        // 将traceId放入MDC
        MDC.put("traceId", traceId);

        //登陆成功就把id存储在ThreadLocal里面
        BaseContext.setCurrentId(Long.parseLong(userId));

        //刷新用户身份信息的缓存时间
        stringRedisTemplate.opsForValue().set(redisKey, userJson, 2, TimeUnit.DAYS);

        Account account = new ObjectMapper().readValue(userJson, Account.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                new LoginUser(account), null, List.of(new SimpleGrantedAuthority(account.getRole())));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.info(SecurityContextHolder.getContext().getAuthentication().toString());

        try {
            filterChain.doFilter(request, response);
        }finally {
            MDC.clear();
        }

    }
    public boolean checkPath(String uri) {
        String[] paths = authProperties.getIncludePaths().toArray(new String[0]);
        for (String path : paths) {
            if (antPathMatcher.match(path, uri)) {
                return true;
            }
        }
        return false;

    }

    private String getTraceIdFromRequest(HttpServletRequest request) {
        return request.getHeader("X-Trace-Id");
    }
}
