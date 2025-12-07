package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 9: Suport tehnic
 * Matches: error, bug, not working, problem, issue, broken, fix
 */
@Component
public class TechnicalSupportRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(error|bug|not\\s*working|problem|issue|broken|fix|crash|fail|doesn't\\s*work)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "🔧 **Technical Support:**\n\n" +
               "If you're experiencing technical issues:\n\n" +
               "**Quick fixes to try:**\n" +
               "1. Refresh the page (Ctrl+F5)\n" +
               "2. Clear browser cache\n" +
               "3. Try a different browser\n" +
               "4. Check your internet connection\n\n" +
               "**Still having problems?**\n" +
               "Contact our technical team at: **support@energy.ro**\n" +
               "Include: error message, browser type, and steps to reproduce.";
    }
    
    @Override
    public int getPriority() {
        return 15;
    }
}
