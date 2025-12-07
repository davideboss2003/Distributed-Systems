package com.energy.websocket.listener;

import com.energy.websocket.dto.ChatMessage;
import com.energy.websocket.handler.ChatWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);
    
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatMessageListener(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @RabbitListener(queues = "chat_queue")
    public void handleChatMessage(ChatMessage message) {
        logger.info("Received chat message from RabbitMQ: {} -> {}", 
            message.getFromUserId(), message.getToUserId());
        
        try {
            if (message.getIsAdmin() != null && message.getIsAdmin()) {
                // Message from admin to client
                logger.info("Sending message from admin to user {}", message.getToUserId());
                chatWebSocketHandler.sendToUser(message.getToUserId(), message);
            } else {
                // Message from client to admin - broadcast to all admins
                logger.info("Broadcasting message from user {} to all admins", message.getFromUserId());
                chatWebSocketHandler.broadcastToAllAdmins(message);
            }
        } catch (Exception e) {
            logger.error("Error handling chat message", e);
        }
    }
}
