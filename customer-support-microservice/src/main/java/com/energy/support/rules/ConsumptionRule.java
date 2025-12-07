package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 5: Întrebări despre consum
 * Matches: consumption, energy, kwh, consum, usage, how much
 */
@Component
public class ConsumptionRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(consumption|energy|kwh|consum|usage|how\\s*much\\s*(energy|power)|electricity)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "📊 **Viewing Energy Consumption:**\n\n" +
               "To check your energy consumption:\n" +
               "1. Go to the **Monitoring** page\n" +
               "2. Select a device from the dropdown\n" +
               "3. Choose a date using the calendar\n" +
               "4. Click **'Load Data'**\n\n" +
               "You'll see an interactive chart showing hourly consumption in kWh. " +
               "The system aggregates data every hour from your smart meters.";
    }
    
    @Override
    public int getPriority() {
        return 11;
    }
}
