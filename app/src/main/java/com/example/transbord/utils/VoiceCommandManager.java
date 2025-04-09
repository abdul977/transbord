package com.example.transbord.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages voice commands and hotword detection for the app
 */
public class VoiceCommandManager implements RecognitionListener {
    private static final String TAG = "VoiceCommandManager";
    private static final String PREF_NAME = "voice_command_prefs";
    private static final String KEY_HOTWORD = "hotword";
    private static final String KEY_COMMANDS_ENABLED = "commands_enabled";
    
    // Default hotword and commands
    private static final String DEFAULT_HOTWORD = "hey transbord";
    private static final Map<String, Integer> DEFAULT_COMMANDS = new HashMap<>();
    
    static {
        // Initialize default commands
        DEFAULT_COMMANDS.put("start recording", 1);
        DEFAULT_COMMANDS.put("stop recording", 2);
        DEFAULT_COMMANDS.put("save", 3);
        DEFAULT_COMMANDS.put("cancel", 4);
        DEFAULT_COMMANDS.put("enhance", 5);
        DEFAULT_COMMANDS.put("format as email", 6);
        DEFAULT_COMMANDS.put("format as list", 7);
        DEFAULT_COMMANDS.put("format as notes", 8);
    }
    
    private final Context context;
    private final SharedPreferences preferences;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isListening = false;
    private final Handler handler;
    
    private String hotword;
    private Map<String, Integer> commands;
    private VoiceCommandListener commandListener;
    
    public interface VoiceCommandListener {
        void onHotwordDetected();
        void onCommandRecognized(int commandId, String command);
        void onListeningStarted();
        void onListeningStopped();
        void onError(int errorCode);
    }
    
    public VoiceCommandManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.handler = new Handler(Looper.getMainLooper());
        
        // Load preferences
        loadPreferences();
        
        // Initialize speech recognizer
        initializeSpeechRecognizer();
    }
    
    private void loadPreferences() {
        hotword = preferences.getString(KEY_HOTWORD, DEFAULT_HOTWORD);
        
        // Initialize commands with defaults
        commands = new HashMap<>(DEFAULT_COMMANDS);
        
        // TODO: Load custom commands from preferences if needed
    }
    
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
            
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        } else {
            Log.e(TAG, "Speech recognition not available on this device");
        }
    }
    
    public void setCommandListener(VoiceCommandListener listener) {
        this.commandListener = listener;
    }
    
    public void startListening() {
        if (speechRecognizer != null && !isListening) {
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
            
            if (commandListener != null) {
                commandListener.onListeningStarted();
            }
        }
    }
    
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            
            if (commandListener != null) {
                commandListener.onListeningStopped();
            }
        }
    }
    
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    
    public boolean isCommandsEnabled() {
        return preferences.getBoolean(KEY_COMMANDS_ENABLED, true);
    }
    
    public void setCommandsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_COMMANDS_ENABLED, enabled).apply();
    }
    
    public String getHotword() {
        return hotword;
    }
    
    public void setHotword(String hotword) {
        this.hotword = hotword;
        preferences.edit().putString(KEY_HOTWORD, hotword).apply();
    }
    
    // Process the recognized speech to detect hotwords and commands
    private void processRecognizedSpeech(List<String> results) {
        if (results.isEmpty()) return;
        
        String speech = results.get(0).toLowerCase();
        Log.d(TAG, "Recognized speech: " + speech);
        
        // Check for hotword
        if (speech.contains(hotword)) {
            if (commandListener != null) {
                commandListener.onHotwordDetected();
            }
            
            // Continue listening for commands
            handler.postDelayed(this::startListening, 1000);
            return;
        }
        
        // Check for commands
        for (Map.Entry<String, Integer> entry : commands.entrySet()) {
            if (speech.contains(entry.getKey())) {
                if (commandListener != null) {
                    commandListener.onCommandRecognized(entry.getValue(), entry.getKey());
                }
                break;
            }
        }
        
        // Continue listening
        handler.postDelayed(this::startListening, 1000);
    }
    
    // RecognitionListener implementation
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "Ready for speech");
    }
    
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech");
    }
    
    @Override
    public void onRmsChanged(float rmsdB) {
        // Not used
    }
    
    @Override
    public void onBufferReceived(byte[] buffer) {
        // Not used
    }
    
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "End of speech");
        isListening = false;
    }
    
    @Override
    public void onError(int error) {
        isListening = false;
        
        if (commandListener != null) {
            commandListener.onError(error);
        }
        
        // Restart listening after a delay
        handler.postDelayed(this::startListening, 2000);
    }
    
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        processRecognizedSpeech(matches);
    }
    
    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String partialText = matches.get(0).toLowerCase();
            if (partialText.contains(hotword)) {
                if (commandListener != null) {
                    commandListener.onHotwordDetected();
                }
            }
        }
    }
    
    @Override
    public void onEvent(int eventType, Bundle params) {
        // Not used
    }
}
