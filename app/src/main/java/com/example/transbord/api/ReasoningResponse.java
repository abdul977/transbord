package com.example.transbord.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReasoningResponse {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("object")
    private String object;
    
    @SerializedName("created")
    private long created;
    
    @SerializedName("model")
    private String model;
    
    @SerializedName("choices")
    private List<Choice> choices;
    
    @SerializedName("usage")
    private Usage usage;
    
    public String getId() {
        return id;
    }
    
    public String getObject() {
        return object;
    }
    
    public long getCreated() {
        return created;
    }
    
    public String getModel() {
        return model;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    // Helper method to get the response content
    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return "";
    }
    
    // Helper method to get the reasoning content if available
    public String getReasoning() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getReasoning();
        }
        return "";
    }
    
    public static class Choice {
        @SerializedName("index")
        private int index;
        
        @SerializedName("message")
        private Message message;
        
        @SerializedName("finish_reason")
        private String finishReason;
        
        public int getIndex() {
            return index;
        }
        
        public Message getMessage() {
            return message;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
    }
    
    public static class Message {
        @SerializedName("role")
        private String role;
        
        @SerializedName("content")
        private String content;
        
        @SerializedName("reasoning")
        private String reasoning;
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
        
        public String getReasoning() {
            return reasoning;
        }
    }
    
    public static class Usage {
        @SerializedName("prompt_tokens")
        private int promptTokens;
        
        @SerializedName("completion_tokens")
        private int completionTokens;
        
        @SerializedName("total_tokens")
        private int totalTokens;
        
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public int getCompletionTokens() {
            return completionTokens;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
    }
}
