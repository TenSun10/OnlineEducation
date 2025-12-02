package com.tenxi.notification.handler;

import com.tenxi.notification.manager.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket处理器
 */
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager webSocketSessionManager;

    public NotificationWebSocketHandler(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
    }

    /**
     * 处理连接断开
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");

        if (userId != null) {
            webSocketSessionManager.removeSession(Long.parseLong(userId));
            log.info("用户 {} WebSocket连接关闭", userId);
        }
    }

    /**
     * 处理连接建立
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");

        if (userId != null) {
            webSocketSessionManager.addSession(Long.parseLong(userId), session);
            log.info("用户 {} WebSocket连接建立", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        log.error("用户 {} WebSocket传输错误", userId, exception);
    }
}
