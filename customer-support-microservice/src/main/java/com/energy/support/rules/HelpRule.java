package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 3: Ajutor general
 * Matches: help, how to, guide, tutorial, what can you do
 */
@Component
public class HelpRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(help|how\\s*to|guide|tutorial|what\\s*can\\s*you\\s*do|ajutor|cum\\s*pot)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "📚 Here's what I can help you with:\n\n" +
               "• **Devices** - Add, edit, or manage your smart devices\n" +
               "• **Consumption** - View energy usage and statistics\n" +
               "• **Billing** - Information about costs and payments\n" +
               "• **Alerts** - Overconsumption notifications\n" +
               "• **Account** - Profile and password management\n" +
               "• **Export** - Download your consumption data\n\n" +
               "Just ask me about any of these topics!";
    }
    
    @Override
    public int getPriority() {
        return 3;
    }
}
