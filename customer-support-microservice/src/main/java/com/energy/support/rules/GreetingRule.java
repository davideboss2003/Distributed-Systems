package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 1: Salutări
 * Matches: hello, hi, hey, bună, salut, good morning, good evening
 */
@Component
public class GreetingRule implements ChatRule {
    
    // REGEX: case-insensitive, caută cuvintele de salut
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(hello|hi|hey|bună|salut|good\\s*morning|good\\s*evening|howdy)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "Hello! 👋 Welcome to Energy Management Support. How can I help you today? " +
               "You can ask me about devices, energy consumption, billing, or type 'help' for more options.";
    }
    
    @Override
    public int getPriority() {
        return 1; // Prioritate mare - salutările sunt importante
    }
}
