package com.tenxi.notification.config;

import com.tenxi.notification.handler.NotificationWebSocketHandler;
import com.tenxi.notification.interceptor.AuthHandshakeInterceptor;
import com.tenxi.notification.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionManager sessionManager;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(sessionManager), "/ws/notification")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
