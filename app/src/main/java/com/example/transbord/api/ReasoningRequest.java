package com.example.transbord.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReasoningRequest {
    
    @SerializedName("model")
    private String model;
    
    @SerializedName("messages")
    private List<Message> messages;
    
    @SerializedName("temperature")
    private double temperature;
    
    @SerializedName("max_completion_tokens")
    private int maxCompletionTokens;
    
    @SerializedName("top_p")
    private double topP;
    
    @SerializedName("reasoning_format")
    private String reasoningFormat;
    
    public ReasoningRequest(String model, List<Message> messages, double temperature, 
                           int maxCompletionTokens, double topP, String reasoningFormat) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.maxCompletionTokens = maxCompletionTokens;
        this.topP = topP;
        this.reasoningFormat = reasoningFormat;
    }
    
    // Builder pattern for creating requests
    public static class Builder {
        private String model = "deepseek-r1-distill-llama-70b"; // Default model
        private List<Message> messages = new ArrayList<>();
        private double temperature = 0.6;
        private int maxCompletionTokens = 1024;
        private double topP = 0.95;
        private String reasoningFormat = "raw";
        
        public Builder() {
        }
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder addMessage(String role, String content) {
            this.messages.add(new Message(role, content));
            return this;
        }
        
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder maxCompletionTokens(int maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }
        
        public Builder topP(double topP) {
            this.topP = topP;
            return this;
        }
        
        public Builder reasoningFormat(String reasoningFormat) {
            this.reasoningFormat = reasoningFormat;
            return this;
        }
        
        public ReasoningRequest build() {
            return new ReasoningRequest(model, messages, temperature, maxCompletionTokens, topP, reasoningFormat);
        }
    }
    
    public static class Message {
        @SerializedName("role")
        private String role;
        
        @SerializedName("content")
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
