package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 6: Alertă overconsumption
 * Matches: overconsumption, exceeded, alert, notification, warning, limit exceeded
 */
@Component
public class OverconsumptionRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(overconsumption|exceeded|alert|notification|warning|limit\\s*exceeded|too\\s*much)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "🚨 **Overconsumption Alerts:**\n\n" +
               "The system automatically monitors your devices and sends real-time alerts " +
               "when consumption exceeds the maximum hourly limit.\n\n" +
               "**How it works:**\n" +
               "• Each device has a 'Max Hourly Consumption' setting\n" +
               "• When hourly usage exceeds this limit, you get an instant notification\n" +
               "• Alerts appear as pop-up notifications in your dashboard\n\n" +
               "To adjust limits, edit the device settings in the Devices page.";
    }
    
    @Override
    public int getPriority() {
        return 12;
    }
}
