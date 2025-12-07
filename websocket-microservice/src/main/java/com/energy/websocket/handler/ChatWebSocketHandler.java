package com.energy.websocket.handler;

import com.energy.websocket.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        logger.info("WebSocket connection established with query: {}", query);
        
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    
                    if ("userId".equals(key)) {
                        userSessions.put(value, session);
                        logger.info("User {} connected to chat", value);
                    } else if ("adminId".equals(key)) {
                        adminSessions.put(value, session);
                        logger.info("Admin {} connected to chat", value);
                    }
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            logger.info("Received chat message from {} to {}", chatMessage.getFromUserId(), chatMessage.getToUserId());
            
            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend("chat_queue", chatMessage);
            logger.info("Published message to chat_queue");
            
        } catch (Exception e) {
            logger.error("Error handling chat message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove session from maps
        userSessions.values().remove(session);
        adminSessions.values().remove(session);
        logger.info("WebSocket connection closed: {}", status);
    }

    public void sendToUser(String userId, ChatMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                logger.info("Sent message to user {}", userId);
            } catch (IOException e) {
                logger.error("Error sending message to user {}", userId, e);
            }
        } else {
            logger.warn("User {} not connected", userId);
        }
    }

    public void sendToAdmin(String adminId, ChatMessage message) {
        WebSocketSession session = adminSessions.get(adminId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                logger.info("Sent message to admin {}", adminId);
            } catch (IOException e) {
                logger.error("Error sending message to admin {}", adminId, e);
            }
        } else {
            logger.warn("Admin {} not connected", adminId);
        }
    }

    public void broadcastToAllAdmins(ChatMessage message) {
        for (Map.Entry<String, WebSocketSession> entry : adminSessions.entrySet()) {
            if (entry.getValue().isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(message);
                    entry.getValue().sendMessage(new TextMessage(json));
                    logger.info("Broadcast message to admin {}", entry.getKey());
                } catch (IOException e) {
                    logger.error("Error broadcasting to admin {}", entry.getKey(), e);
                }
            }
        }
    }
}
