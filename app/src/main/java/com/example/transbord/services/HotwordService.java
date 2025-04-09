package com.example.transbord.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.transbord.MainActivity;
import com.example.transbord.R;
import com.example.transbord.utils.VoiceCommandManager;

/**
 * Service for detecting hotwords and voice commands in the background
 */
public class HotwordService extends Service implements VoiceCommandManager.VoiceCommandListener {
    private static final String TAG = "HotwordService";
    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "hotword_channel";

    private VoiceCommandManager voiceCommandManager;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize voice command manager
        voiceCommandManager = new VoiceCommandManager(this);
        voiceCommandManager.setCommandListener(this);

        // Create notification channel for Android 8.0+
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (!isRunning) {
                // Start as a foreground service
                startForeground(NOTIFICATION_ID, createNotification());

                // Start listening for hotwords
                if (voiceCommandManager.isCommandsEnabled()) {
                    Log.d(TAG, "Starting voice command listening");
                    voiceCommandManager.startListening();
                    isRunning = true;
                    Toast.makeText(this, R.string.voice_commands_activated, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Voice commands are disabled in preferences");
                    stopSelf();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting hotword service: " + e.getMessage());
            Toast.makeText(this, "Error starting voice commands: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (voiceCommandManager != null) {
            voiceCommandManager.stopListening();
            voiceCommandManager.destroy();
        }

        isRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hotword Detection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Used for detecting voice commands");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.voice_commands_active))
                .setContentText(getString(R.string.listening_for_hotword, voiceCommandManager.getHotword()))
                .setSmallIcon(R.drawable.ic_mic)
                .setContentIntent(pendingIntent)
                .build();
    }

    // VoiceCommandListener implementation
    @Override
    public void onHotwordDetected() {
        Log.d(TAG, "Hotword detected: " + voiceCommandManager.getHotword());
        Toast.makeText(this, R.string.hotword_detected, Toast.LENGTH_SHORT).show();

        // Play a sound or vibrate to indicate hotword detection
        // TODO: Add sound/vibration feedback
    }

    @Override
    public void onCommandRecognized(int commandId, String command) {
        Log.d(TAG, "Command recognized: " + command + " (ID: " + commandId + ")");
        Toast.makeText(this, getString(R.string.command_recognized, command), Toast.LENGTH_SHORT).show();

        // Handle different commands
        handleCommand(commandId);
    }

    private void handleCommand(int commandId) {
        Intent intent;

        switch (commandId) {
            case 1: // Start recording
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_START_RECORDING");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 2: // Stop recording
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_STOP_RECORDING");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 3: // Save
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_SAVE_TRANSCRIPTION");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 4: // Cancel
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_CANCEL");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 5: // Enhance
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_ENHANCE");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 6: // Format as email
            case 7: // Format as list
            case 8: // Format as notes
                intent = new Intent(this, MainActivity.class);
                intent.setAction("ACTION_FORMAT");
                intent.putExtra("format_type", commandId - 5); // 1=email, 2=list, 3=notes
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onListeningStarted() {
        Log.d(TAG, "Listening started");
    }

    @Override
    public void onListeningStopped() {
        Log.d(TAG, "Listening stopped");
    }

    @Override
    public void onError(int errorCode) {
        Log.e(TAG, "Speech recognition error: " + errorCode);

        if (errorCode == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            Toast.makeText(this, R.string.microphone_permission_required, Toast.LENGTH_LONG).show();
        }
    }
}
