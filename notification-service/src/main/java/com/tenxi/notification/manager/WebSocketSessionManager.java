package com.tenxi.notification.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户会话管理器
 * 实现连接的处理和通知的发送
 */
@Component
public class WebSocketSessionManager {
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    //添加连接
    public void addSession(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    //删除连接
    public void removeSession(Long userId) {
        sessions.remove(userId);
    }

    //根据id获取连接
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    //判断用户的WebSocket连接是否开通
    public boolean isUserConnected(Long userId) {
        return sessions.containsKey(userId) && sessions.get(userId).isOpen();
    }
}
