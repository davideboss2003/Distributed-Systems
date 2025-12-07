package com.energy.support.service;

import java.util.regex.Pattern;

/**
 * Interface pentru regulile chatbot-ului
 * 
 * Fiecare regulă definește:
 * 1. Un pattern REGEX pentru matching
 * 2. Un răspuns predefinit
 * 3. O prioritate (mai mică = mai importantă)
 */
public interface ChatRule {
    
    /**
     * Returnează pattern-ul REGEX pentru această regulă
     * Exemplu: "(?i).*\\b(hello|hi|hey)\\b.*"
     */
    Pattern getPattern();
    
    /**
     * Verifică dacă mesajul se potrivește cu pattern-ul
     */
    default boolean matches(String message) {
        return getPattern().matcher(message).matches();
    }
    
    /**
     * Returnează răspunsul pentru această regulă
     */
    String getResponse();
    
    /**
     * Returnează prioritatea (1-100, mai mic = prioritate mai mare)
     */
    int getPriority();
    
    /**
     * Returnează numele regulii (pentru debugging)
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
