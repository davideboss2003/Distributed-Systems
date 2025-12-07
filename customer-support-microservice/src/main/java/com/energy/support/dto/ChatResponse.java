package com.energy.support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pentru response-ul de chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;           // Răspunsul chatbot-ului
    private boolean isRuleBased;       // true dacă a folosit regulă, false dacă AI
    private String ruleName;           // Numele regulii care a răspuns (pentru debugging)
    private LocalDateTime timestamp;   // Când a fost generat răspunsul
    
    /**
     * Constructor convenience pentru răspunsuri rule-based
     */
    public static ChatResponse fromRule(String response, String ruleName) {
        return new ChatResponse(response, true, ruleName, LocalDateTime.now());
    }
    
    /**
     * Constructor convenience pentru răspunsuri AI
     */
    public static ChatResponse fromAI(String response, String aiModel) {
        return new ChatResponse(response, false, aiModel, LocalDateTime.now());
    }
    
    /**
     * Constructor convenience pentru răspunsuri default (nicio regulă, AI eșuat)
     */
    public static ChatResponse defaultResponse(String response) {
        return new ChatResponse(response, false, "DefaultResponse", LocalDateTime.now());
    }
}
