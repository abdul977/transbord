package com.example.transbord.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages templates and formatting for transcriptions
 */
public class TemplateManager {
    private static final String TAG = "TemplateManager";
    private static final String PREF_NAME = "template_prefs";
    private static final String KEY_CUSTOM_TEMPLATES = "custom_templates";
    
    // Template types
    public static final int TEMPLATE_EMAIL = 1;
    public static final int TEMPLATE_LIST = 2;
    public static final int TEMPLATE_NOTES = 3;
    public static final int TEMPLATE_MEETING = 4;
    public static final int TEMPLATE_CUSTOM = 5;
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Map<Integer, String> templatePatterns;
    private final Map<Integer, String> templateFormats;
    private final List<CustomTemplate> customTemplates;
    
    public static class CustomTemplate {
        private final String name;
        private final String pattern;
        private final String format;
        
        public CustomTemplate(String name, String pattern, String format) {
            this.name = name;
            this.pattern = pattern;
            this.format = format;
        }
        
        public String getName() {
            return name;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public String getFormat() {
            return format;
        }
    }
    
    public TemplateManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.templatePatterns = new HashMap<>();
        this.templateFormats = new HashMap<>();
        this.customTemplates = new ArrayList<>();
        
        // Initialize default templates
        initializeDefaultTemplates();
        
        // Load custom templates
        loadCustomTemplates();
    }
    
    private void initializeDefaultTemplates() {
        // Email pattern and format
        templatePatterns.put(TEMPLATE_EMAIL, 
                "(?i).*(email|mail|message|send|to:|from:|subject:|dear|sincerely|regards).*");
        templateFormats.put(TEMPLATE_EMAIL, 
                "To: [Recipient]\nFrom: [Sender]\nSubject: [Subject]\n\n%s\n\nRegards,\n[Your Name]");
        
        // List pattern and format
        templatePatterns.put(TEMPLATE_LIST, 
                "(?i).*(list|items|bullet points|numbered|first|second|third|next).*");
        templateFormats.put(TEMPLATE_LIST, 
                "• %s");
        
        // Notes pattern and format
        templatePatterns.put(TEMPLATE_NOTES, 
                "(?i).*(note|notes|remember|don't forget|reminder|important).*");
        templateFormats.put(TEMPLATE_NOTES, 
                "Note: %s");
        
        // Meeting pattern and format
        templatePatterns.put(TEMPLATE_MEETING, 
                "(?i).*(meeting|agenda|discussion|participants|attendees|minutes).*");
        templateFormats.put(TEMPLATE_MEETING, 
                "Meeting Notes\n\nDate: [Date]\nParticipants: [Participants]\n\nAgenda:\n1. %s\n\nAction Items:\n• [Action Item 1]\n• [Action Item 2]");
    }
    
    private void loadCustomTemplates() {
        // TODO: Implement loading custom templates from SharedPreferences
    }
    
    /**
     * Detect the appropriate template type for the given text
     * @param text The text to analyze
     * @return The detected template type or 0 if no template is detected
     */
    public int detectTemplateType(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // Check built-in templates
        for (Map.Entry<Integer, String> entry : templatePatterns.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue());
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return entry.getKey();
            }
        }
        
        // Check custom templates
        for (int i = 0; i < customTemplates.size(); i++) {
            CustomTemplate template = customTemplates.get(i);
            Pattern pattern = Pattern.compile(template.getPattern());
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return TEMPLATE_CUSTOM + i; // Custom template IDs start after the built-in ones
            }
        }
        
        return 0; // No template detected
    }
    
    /**
     * Format text according to the specified template type
     * @param text The text to format
     * @param templateType The template type to apply
     * @return The formatted text
     */
    public String formatText(String text, int templateType) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        if (templateType == 0) {
            return text; // No formatting needed
        }
        
        try {
            if (templateType >= TEMPLATE_CUSTOM) {
                // Custom template
                int customIndex = templateType - TEMPLATE_CUSTOM;
                if (customIndex < customTemplates.size()) {
                    CustomTemplate template = customTemplates.get(customIndex);
                    return applyFormat(text, template.getFormat());
                }
            } else {
                // Built-in template
                String format = templateFormats.get(templateType);
                if (format != null) {
                    return applyFormat(text, format);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting text: " + e.getMessage());
        }
        
        return text; // Return original text if formatting fails
    }
    
    /**
     * Apply a format string to the text
     * @param text The text to format
     * @param format The format string
     * @return The formatted text
     */
    private String applyFormat(String text, String format) {
        if (format.contains("%s")) {
            // Simple format with placeholder
            return String.format(format, text);
        } else {
            // No placeholder, just append
            return format + "\n" + text;
        }
    }
    
    /**
     * Format text as a list
     * @param text The text to format
     * @return The formatted list
     */
    public String formatAsList(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Split text by lines or sentences
        String[] lines = text.split("(?<=\\.)\\s+|\\n");
        StringBuilder formattedText = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                formattedText.append("• ").append(line).append("\n");
            }
        }
        
        return formattedText.toString();
    }
    
    /**
     * Format text as an email
     * @param text The text to format
     * @return The formatted email
     */
    public String formatAsEmail(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder email = new StringBuilder();
        email.append("To: [Recipient]\n");
        email.append("From: [Sender]\n");
        email.append("Subject: [Subject]\n\n");
        email.append(text).append("\n\n");
        email.append("Regards,\n");
        email.append("[Your Name]");
        
        return email.toString();
    }
    
    /**
     * Format text as meeting notes
     * @param text The text to format
     * @return The formatted meeting notes
     */
    public String formatAsMeetingNotes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder notes = new StringBuilder();
        notes.append("Meeting Notes\n\n");
        notes.append("Date: [Date]\n");
        notes.append("Participants: [Participants]\n\n");
        notes.append("Discussion:\n");
        
        // Split text by lines or sentences
        String[] lines = text.split("(?<=\\.)\\s+|\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                notes.append(i + 1).append(". ").append(line).append("\n");
            }
        }
        
        notes.append("\nAction Items:\n");
        notes.append("• [Action Item 1]\n");
        notes.append("• [Action Item 2]\n");
        
        return notes.toString();
    }
    
    /**
     * Add a new custom template
     * @param name The template name
     * @param pattern The detection pattern
     * @param format The format string
     */
    public void addCustomTemplate(String name, String pattern, String format) {
        CustomTemplate template = new CustomTemplate(name, pattern, format);
        customTemplates.add(template);
        saveCustomTemplates();
    }
    
    /**
     * Remove a custom template
     * @param index The index of the template to remove
     */
    public void removeCustomTemplate(int index) {
        if (index >= 0 && index < customTemplates.size()) {
            customTemplates.remove(index);
            saveCustomTemplates();
        }
    }
    
    /**
     * Get all custom templates
     * @return The list of custom templates
     */
    public List<CustomTemplate> getCustomTemplates() {
        return customTemplates;
    }
    
    /**
     * Save custom templates to preferences
     */
    private void saveCustomTemplates() {
        // TODO: Implement saving custom templates to SharedPreferences
    }
}
