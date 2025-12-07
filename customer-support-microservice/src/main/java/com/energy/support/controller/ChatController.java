package com.energy.support.controller;

import com.energy.support.dto.ChatRequest;
import com.energy.support.dto.ChatResponse;
import com.energy.support.dto.ChatMessageRequest;
import com.energy.support.dto.ChatMessageResponse;
import com.energy.support.entity.ChatMessageEntity;
import com.energy.support.service.RuleBasedChatService;
import com.energy.support.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pentru Chat API
 * 
 * Endpoints:
 * - POST /api/chat/message - Trimite mesaj și primește răspuns
 * - GET /api/chat/rules - Lista reguli (pentru debugging)
 * - GET /api/chat/health - Health check
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost"})
public class ChatController {
    
    private final RuleBasedChatService chatService;
    private final ChatMessageService chatMessageService;
    
    /**
     * POST /api/chat/message
     * Trimite un mesaj și primește răspunsul chatbot-ului
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        log.info("📩 Received chat message: '{}' from user: {}", 
                 request.getMessage(), request.getUserId());
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ChatResponse.defaultResponse("Please enter a message.")
            );
        }
        
        ChatResponse response = chatService.processMessage(request.getMessage().trim());
        
        log.info("📤 Sending response: {} (rule: {})", 
                 response.isRuleBased() ? "Rule-based" : "Default", 
                 response.getRuleName());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/chat/rules
     * Returnează lista de reguli disponibile (pentru debugging)
     */
    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getRules() {
        List<String> rules = chatService.getAllRuleNames();
        return ResponseEntity.ok(Map.of(
            "totalRules", rules.size(),
            "rules", rules
        ));
    }
    
    /**
     * GET /api/chat/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "customer-support-microservice"
        ));
    }
    
    /**
     * POST /api/chat/send
     * Trimite mesaj în chat client-admin
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendChatMessage(@RequestBody ChatMessageRequest request) {
        log.info("📩 Sending chat message from {} to {}", request.getFromUserId(), request.getToUserId());
        
        try {
            chatMessageService.sendMessage(request);
            return ResponseEntity.ok(new ChatMessageResponse(true, "Message sent successfully"));
        } catch (Exception e) {
            log.error("Error sending chat message", e);
            return ResponseEntity.internalServerError()
                .body(new ChatMessageResponse(false, "Failed to send message: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/chat/history/{userId}
     * Returnează istoricul conversației pentru un user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessageEntity>> getChatHistory(@PathVariable String userId) {
        log.info("📜 Fetching chat history for user: {}", userId);
        List<ChatMessageEntity> history = chatMessageService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * GET /api/chat/conversation/{userId1}/{userId2}
     * Returnează conversația între doi useri
     */
    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<List<ChatMessageEntity>> getConversation(
            @PathVariable String userId1, 
            @PathVariable String userId2) {
        log.info("📜 Fetching conversation between {} and {}", userId1, userId2);
        List<ChatMessageEntity> conversation = chatMessageService.getConversation(userId1, userId2);
        return ResponseEntity.ok(conversation);
    }
}
