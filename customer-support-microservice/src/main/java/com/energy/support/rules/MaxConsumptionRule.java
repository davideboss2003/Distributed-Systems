package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 11: Limită consum maxim
 * Matches: max consumption, limit, threshold, maximum, set limit
 */
@Component
public class MaxConsumptionRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(max\\s*consumption|limit|threshold|maximum|set\\s*limit|hourly\\s*limit)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "⚡ **Setting Maximum Consumption Limits:**\n\n" +
               "Each device can have a maximum hourly consumption limit:\n\n" +
               "**To set or change the limit:**\n" +
               "1. Go to **Devices** page\n" +
               "2. Click **Edit** on the device\n" +
               "3. Set the **'Max Hourly Consumption'** value (in kWh)\n" +
               "4. Save changes\n\n" +
               "When consumption exceeds this limit, you'll receive an instant alert. " +
               "Recommended: Set limit 10-20% above normal usage.";
    }
    
    @Override
    public int getPriority() {
        return 17;
    }
}
