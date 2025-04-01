package com.tenxi.interceptor;

import com.tenxi.entity.LoginUser;
import com.tenxi.utils.HmacSigner;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 用于处理Open Feign之间发送请求的拦截器
 * 目的是将用户的id传递，用于后续的验证
 */
@Component
public class FeignClientAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //从SecurityContextHolder中获取用户的身份
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            //将用户的身份id存储在请求头中,以便后续验证
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String userId = loginUser.getAccount().getId().toString();
            //生成签名，方便验证请求的来源
            String signature = HmacSigner.sign(userId);
            requestTemplate.header("X-Signature", signature)
                    .header("X-User-Id", userId);
        }
    }
}
