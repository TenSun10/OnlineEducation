package com.tenxi.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface WebSocketService {
    void sendToUser(Long userId, Object message) throws JsonProcessingException;

    boolean isUserOnline(Long userId);
}
