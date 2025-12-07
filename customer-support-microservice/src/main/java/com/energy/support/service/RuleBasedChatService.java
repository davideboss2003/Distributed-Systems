package com.energy.support.service;

import com.energy.support.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pentru procesarea mesajelor cu reguli + AI fallback
 * 
 * Flow:
 * 1. Primește mesaj de la utilizator
 * 2. Sortează regulile după prioritate
 * 3. Verifică fiecare regulă (REGEX matching)
 * 4. Prima regulă care se potrivește → returnează răspunsul
 * 5. Dacă nicio regulă nu se potrivește → încearcă AI (Gemini)
 * 6. Dacă AI eșuează → răspuns default
 */
@Slf4j
@Service
public class RuleBasedChatService {
    
    private final List<ChatRule> rules;
    private final GeminiService geminiService;
    
    private static final String DEFAULT_RESPONSE = 
        "I'm sorry, I didn't understand your question. " +
        "You can ask me about: devices, consumption, billing, account, or type 'help' for more options.";
    
    /**
     * Constructor - Spring injectează automat toate bean-urile care implementează ChatRule
     */
    public RuleBasedChatService(List<ChatRule> rules, GeminiService geminiService) {
        this.geminiService = geminiService;
        this.rules = rules.stream()
            .sorted(Comparator.comparingInt(ChatRule::getPriority))
            .collect(Collectors.toList());
        
        log.info("✅ Loaded {} chat rules:", rules.size());
        this.rules.forEach(rule -> 
            log.info("   - {} (priority: {})", rule.getName(), rule.getPriority())
        );
        
        if (geminiService.isAvailable()) {
            log.info("✅ Gemini AI fallback is ENABLED");
        } else {
            log.warn("⚠️ Gemini AI fallback is DISABLED (no API key)");
        }
    }
    
    /**
     * Procesează un mesaj și returnează răspunsul
     */
    public ChatResponse processMessage(String message) {
        log.debug("Processing message: '{}'", message);
        
        // 1. Verifică fiecare regulă (sortate după prioritate)
        for (ChatRule rule : rules) {
            if (rule.matches(message)) {
                log.info("✅ Rule matched: {} for message: '{}'", rule.getName(), message);
                return ChatResponse.fromRule(rule.getResponse(), rule.getName());
            }
        }
        
        // 2. Nicio regulă nu s-a potrivit - încearcă AI
        log.info("❌ No rule matched for message: '{}', trying AI...", message);
        
        if (geminiService.isAvailable()) {
            try {
                String aiResponse = geminiService.generateResponse(message);
                if (aiResponse != null && !aiResponse.isEmpty()) {
                    log.info("✅ AI response generated successfully");
                    return ChatResponse.fromAI(aiResponse, "GeminiAI");
                }
            } catch (Exception e) {
                log.error("❌ AI service failed: {}", e.getMessage());
            }
        }
        
        // 3. AI nu e disponibil sau a eșuat - răspuns default
        log.info("Returning default response");
        return ChatResponse.defaultResponse(DEFAULT_RESPONSE);
    }
    
    /**
     * Returnează lista de reguli (pentru debugging)
     */
    public List<String> getAllRuleNames() {
        return rules.stream()
            .map(rule -> rule.getName() + " (priority: " + rule.getPriority() + ")")
            .collect(Collectors.toList());
    }
}
