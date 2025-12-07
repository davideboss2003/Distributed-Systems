package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 4: Informații despre device-uri
 * Matches: device, add device, create device, new device, dispozitiv
 */
@Component
public class DeviceInfoRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(device|devices|add\\s*device|create\\s*device|new\\s*device|dispozitiv|smart\\s*meter)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "🔌 **Managing Devices:**\n\n" +
               "To add a new device:\n" +
               "1. Go to the **Devices** page from the sidebar\n" +
               "2. Click the **'Add Device'** button\n" +
               "3. Fill in: Description, Address, Max Hourly Consumption\n" +
               "4. Assign it to a user\n" +
               "5. Click **Save**\n\n" +
               "You can also edit or delete existing devices from the same page.";
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
}
