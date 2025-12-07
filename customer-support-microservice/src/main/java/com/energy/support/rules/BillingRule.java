package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 8: Facturare
 * Matches: bill, payment, invoice, cost, price, money, pay
 */
@Component
public class BillingRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(bill|billing|payment|invoice|cost|price|money|pay|charge|fee|tariff)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "💰 **Billing Information:**\n\n" +
               "Your energy bill is calculated based on:\n" +
               "• **Hourly consumption** recorded by your smart meters\n" +
               "• **Current tariff** rates from your energy provider\n\n" +
               "To estimate your costs:\n" +
               "1. Check your total consumption in the Monitoring page\n" +
               "2. Multiply by your provider's rate (typically 0.15-0.25 €/kWh)\n\n" +
               "For detailed billing inquiries, contact your energy provider directly.";
    }
    
    @Override
    public int getPriority() {
        return 14;
    }
}
