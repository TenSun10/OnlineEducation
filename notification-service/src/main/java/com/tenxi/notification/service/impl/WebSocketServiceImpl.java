package com.tenxi.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxi.notification.service.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Resource
    private SimpMessagingTemplate template;
    @Resource
    private ObjectMapper objectMapper;

    public WebSocketServiceImpl() {
    }

    public void sendToUser(Long userId, Object message) throws JsonProcessingException {
        String destination = "/user/" + userId + "/notification";
        String json = objectMapper.writeValueAsString(message);
        this.template.convertAndSend(destination, json);
    }
}
