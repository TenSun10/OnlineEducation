package com.tenxi.notification.interceptor;

import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.JwtException;
import com.tenxi.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 在使用WebSocket连接的通道时进行身份验证
 */
@Slf4j
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String authorization = extractAuthorization(request);

        if (authorization != null) {
            try {
                //1.从jwt中解析出userId
                Claims claims = JwtUtils.parseJwt(authorization);
                String userId = (String) claims.get("user_id");
                //2.将userId放入session，方便后续使用
                attributes.put("userId", userId);

                return true;
            }catch (Exception e) {
                throw new JwtException(ErrorCode.JWT_INVALID_FAILED);
            }
        }
        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    /**
     * 从参数中获取到验证身份的jwt
     * @param request
     * @return
     */
    private String extractAuthorization(ServerHttpRequest request) {
        String query =  request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("authorization=")) {
                    return param.substring("authorization=".length());
                }
            }
        }
        return null;
    }
}
