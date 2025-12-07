package com.energy.support.rules;

import com.energy.support.service.ChatRule;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Regula 10: Export date
 * Matches: export, download, csv, report, data
 */
@Component
public class DataExportRule implements ChatRule {
    
    private static final Pattern PATTERN = Pattern.compile(
        "(?i).*\\b(export|download|csv|report|data\\s*export|get\\s*data|save\\s*data)\\b.*"
    );
    
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }
    
    @Override
    public String getResponse() {
        return "📥 **Exporting Data:**\n\n" +
               "To export your consumption data:\n" +
               "1. Go to the **Monitoring** page\n" +
               "2. Select your device and date range\n" +
               "3. Load the data\n" +
               "4. Click the **Export** button (if available)\n\n" +
               "Data is exported in CSV format which can be opened in Excel or Google Sheets. " +
               "For bulk data exports, contact your administrator.";
    }
    
    @Override
    public int getPriority() {
        return 16;
    }
}
