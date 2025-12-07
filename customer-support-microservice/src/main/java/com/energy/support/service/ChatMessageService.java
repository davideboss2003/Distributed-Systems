package com.energy.support.service;

import com.energy.support.dto.ChatMessageRequest;
import com.energy.support.entity.ChatMessageEntity;
import com.energy.support.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);
    
    private final ChatMessageRepository chatMessageRepository;
    private final RabbitTemplate rabbitTemplate;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, RabbitTemplate rabbitTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(ChatMessageRequest request) {
        // Save to database
        ChatMessageEntity entity = new ChatMessageEntity(
            request.getFromUserId(),
            request.getToUserId(),
            request.getMessage(),
            request.getIsAdmin()
        );
        chatMessageRepository.save(entity);
        logger.info("Saved chat message to database");

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend("chat_queue", request);
        logger.info("Published chat message to RabbitMQ");
    }

    public List<ChatMessageEntity> getChatHistory(String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampDesc(userId)
            .stream()
            .limit(50)
            .toList();
    }

    public List<ChatMessageEntity> getConversation(String userId1, String userId2) {
        return chatMessageRepository.findConversationBetweenUsers(userId1, userId2)
            .stream()
            .limit(50)
            .toList();
    }
}
