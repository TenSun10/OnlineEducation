package com.tenxi.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxi.notification.manager.WebSocketSessionManager;
import com.tenxi.notification.service.WebSocketService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Resource
    private WebSocketSessionManager sessionManager;
    @Resource
    private ObjectMapper objectMapper;

    public WebSocketServiceImpl() {
    }

    public void sendToUser(Long userId, Object message) throws JsonProcessingException {
        //用户WebSocket通道连通
        if (sessionManager.isUserConnected(userId)) {
            WebSocketSession session = sessionManager.getSession(userId);

            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                        log.info("成功向用户 {} 发送WebSocket通知", userId);
                    }else {
                        log.error("向用户 {} 发送WebSocket消息失败", userId);
                    }
                }
            } catch (IOException e) {
                log.error("向用户 {} 发送WebSocket消息失败", userId, e);
            }
        }else {
            log.warn("用户 {} 未建立WebSocket连接，无法发送实时通知", userId);
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return sessionManager.isUserConnected(userId);
    }
}
