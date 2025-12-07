package com.energy.support.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service pentru integrare cu Google Gemini AI
 * 
 * Apelează Gemini API pentru a genera răspunsuri inteligente
 * când nicio regulă nu se potrivește cu mesajul utilizatorului.
 */
@Slf4j
@Service
public class GeminiService {
    
    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final int timeout;
    private final boolean enabled;
    
    private static final String SYSTEM_PROMPT = """
        You are a helpful customer support assistant for an Energy Management System.
        Your role is to help users with:
        - Managing smart energy devices
        - Viewing energy consumption data
        - Understanding overconsumption alerts
        - Billing and payment questions
        - Account management
        
        Keep responses concise, friendly, and helpful.
        If asked about something outside energy management, politely redirect to energy-related topics.
        Use emojis sparingly to make responses more engaging.
        """;
    
    public GeminiService(
            @Value("${gemini.apiKey}") String apiKey,
            @Value("${gemini.apiUrl}") String apiUrl,
            @Value("${gemini.timeout:15000}") int timeout,
            @Value("${gemini.enabled:true}") boolean enabled) {
        
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.timeout = timeout;
        this.enabled = enabled;
        
        this.webClient = WebClient.builder()
            .defaultHeader("Content-Type", "application/json")
            .build();
        
        System.out.println("🔑🔑🔑 GEMINI API KEY LENGTH: " + (apiKey != null ? apiKey.length() : 0));
        System.out.println("🔑🔑🔑 GEMINI API KEY VALUE: '" + apiKey + "'");
        log.warn("🔑🔑🔑 Gemini API Key length: {}", apiKey != null ? apiKey.length() : 0);
        
        if (enabled && (apiKey == null || apiKey.isEmpty())) {
            log.warn("⚠️ Gemini API key not configured. AI responses will be disabled.");
        } else if (enabled) {
            log.info("✅ Gemini AI Service initialized successfully");
        }
    }

    /**
     * Verifică dacă serviciul AI este disponibil
     */
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Generează un răspuns folosind Gemini AI
     * 
     * @param userMessage Mesajul utilizatorului
     * @return Răspunsul generat de AI
     */
    public String generateResponse(String userMessage) {
        if (!isAvailable()) {
            log.warn("Gemini AI not available - returning default response");
            return null;
        }
        
        log.info(" Calling Gemini AI for message: '{}'", userMessage);
        
        try {
            // Construiește prompt-ul complet
            String fullPrompt = SYSTEM_PROMPT + "\n\nUser question: " + userMessage + 
                               "\n\nProvide a helpful, concise response:";
            
            // Construiește request body pentru Gemini API
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", fullPrompt)
                    ))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 500,
                    "topP", 0.9
                )
            );
            
            // Apelează Gemini API
            String url = apiUrl + "?key=" + apiKey;
            
            Map<String, Object> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            // Extrage răspunsul din response
            String aiResponse = extractResponseText(response);
            
            log.info(" Gemini AI response received: {} chars", 
                     aiResponse != null ? aiResponse.length() : 0);
            
            return aiResponse;
            
        } catch (Exception e) {
            log.error(" Gemini AI call failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrage textul răspunsului din response-ul Gemini API
     */
    @SuppressWarnings("unchecked")
    private String extractResponseText(Map<String, Object> response) {
        try {
            if (response == null) return null;
            
            List<Map<String, Object>> candidates = 
                (List<Map<String, Object>>) response.get("candidates");
            
            if (candidates == null || candidates.isEmpty()) return null;
            
            Map<String, Object> content = 
                (Map<String, Object>) candidates.get(0).get("content");
            
            if (content == null) return null;
            
            List<Map<String, Object>> parts = 
                (List<Map<String, Object>>) content.get("parts");
            
            if (parts == null || parts.isEmpty()) return null;
            
            return (String) parts.get(0).get("text");
            
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return null;
        }
    }
}
