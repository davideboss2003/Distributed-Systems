package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 2: La revedere / Mulțumiri
 * Matches: bye, goodbye, thanks, thank you, pa, mulțumesc
 */
@Component
public class FarewellRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(bye|goodbye|thanks|thank\\s*you|pa|mulțumesc|multumesc|see\\s*you|take\\s*care)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "You're welcome! 😊 Thank you for using Energy Management Support. " +
               "Have a great day! If you need help again, I'm always here.";
    }
    
    @Override
    public int getPriority() {
        return 2;
    }
}
