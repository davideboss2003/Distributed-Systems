package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 7: Informații cont
 * Matches: account, profile, password, email, settings, user
 */
@Component
public class AccountRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(account|profile|password|email|settings|user|login|register|sign\\s*up)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "👤 **Account Management:**\n\n" +
               "• **Login**: Use your email and password at the login page\n" +
               "• **Register**: Click 'Register here' on the login page\n" +
               "• **Profile**: View your account details in the sidebar\n" +
               "• **Password**: Contact admin to reset your password\n\n" +
               "For security reasons, passwords are encrypted and cannot be recovered. " +
               "If you forgot your password, please contact support@energy.ro";
    }
    
    @Override
    public int getPriority() {
        return 13;
    }
}
