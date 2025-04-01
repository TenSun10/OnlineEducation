package com.tenxi.filter;

import com.tenxi.utils.HmacSigner;
import com.tenxi.utils.JwtUtils;
import com.tenxi.config.AuthProperties;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 注意：这个类不需要在配置文件中配置
 * GATEWAY会自动扫描
 * 如果在配置文件中写了会报错
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private AuthProperties authProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();  // 获取纯路径（不带参数）

        log.info("拦截到请求: {}", path);
        // 检查路径是否需要跳过验证
        if (checkPath(path)) {
            log.info("路径无需验证: {}", path);
            return chain.filter(exchange);
        }

        // 检查 JWT
        String jwt = exchange.getRequest().getHeaders().getFirst("authorization");

        Claims claims;
        String user_id;
        try {
            claims = JwtUtils.parseJwt(jwt);
            user_id = claims.get("user_id").toString();
        } catch (Exception e) {
            //拦截，设置状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //用用户的id生成签名，方便验证请求的来源，防止中间修改请求头
        String signature = HmacSigner.sign(user_id);
        // 传递用户 ID
        exchange.getRequest().mutate()
                .header("X-User-Id", user_id)
                .header("X-Signature", signature)
                .build();

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;  // 提高优先级
    }

    private boolean checkPath(String path) {
        return authProperties.getIncludePaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }
}