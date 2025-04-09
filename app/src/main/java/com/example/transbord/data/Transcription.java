package com.example.transbord.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "transcriptions")
public class Transcription {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @NonNull
    private String text;
    
    private String audioFilePath;
    
    private long timestamp;
    
    private String title;
    
    // Constructor
    public Transcription(@NonNull String text, String audioFilePath, long timestamp, String title) {
        this.text = text;
        this.audioFilePath = audioFilePath;
        this.timestamp = timestamp;
        this.title = title;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @NonNull
    public String getText() {
        return text;
    }
    
    public void setText(@NonNull String text) {
        this.text = text;
    }
    
    public String getAudioFilePath() {
        return audioFilePath;
    }
    
    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    // Helper method to generate a title from text
    public static String generateTitleFromText(String text) {
        if (text == null || text.isEmpty()) {
            return "Untitled";
        }
        
        // Use the first few words as the title
        String[] words = text.split("\\s+");
        StringBuilder titleBuilder = new StringBuilder();
        
        int wordCount = Math.min(5, words.length);
        for (int i = 0; i < wordCount; i++) {
            titleBuilder.append(words[i]).append(" ");
        }
        
        String title = titleBuilder.toString().trim();
        if (title.length() > 30) {
            title = title.substring(0, 30) + "...";
        }
        
        return title;
    }
}
