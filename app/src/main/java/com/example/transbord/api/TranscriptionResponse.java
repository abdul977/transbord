package com.example.transbord.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TranscriptionResponse {
    
    @SerializedName("text")
    private String text;
    
    @SerializedName("segments")
    private List<Segment> segments;
    
    public String getText() {
        return text;
    }
    
    public List<Segment> getSegments() {
        return segments;
    }
    
    public static class Segment {
        @SerializedName("id")
        private int id;
        
        @SerializedName("seek")
        private int seek;
        
        @SerializedName("start")
        private double start;
        
        @SerializedName("end")
        private double end;
        
        @SerializedName("text")
        private String text;
        
        @SerializedName("tokens")
        private List<Integer> tokens;
        
        @SerializedName("temperature")
        private double temperature;
        
        @SerializedName("avg_logprob")
        private double avgLogprob;
        
        @SerializedName("compression_ratio")
        private double compressionRatio;
        
        @SerializedName("no_speech_prob")
        private double noSpeechProb;
        
        // Getters
        public int getId() {
            return id;
        }
        
        public int getSeek() {
            return seek;
        }
        
        public double getStart() {
            return start;
        }
        
        public double getEnd() {
            return end;
        }
        
        public String getText() {
            return text;
        }
        
        public List<Integer> getTokens() {
            return tokens;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public double getAvgLogprob() {
            return avgLogprob;
        }
        
        public double getCompressionRatio() {
            return compressionRatio;
        }
        
        public double getNoSpeechProb() {
            return noSpeechProb;
        }
    }
}
