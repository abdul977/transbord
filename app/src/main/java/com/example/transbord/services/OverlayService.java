package com.example.transbord.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.transbord.MainActivity;
import com.example.transbord.R;
import com.example.transbord.TranscriptionActivity;
import com.example.transbord.api.GroqApiClient;
import com.example.transbord.api.TranscriptionResponse;
import com.example.transbord.data.Transcription;
import com.example.transbord.data.TranscriptionRepository;
import com.example.transbord.utils.AudioRecorder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class OverlayService extends Service {
    private static final String TAG = "OverlayService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "TransbordOverlayChannel";

    private WindowManager windowManager;
    private View overlayView;
    private FloatingActionButton fabOverlayRecord;

    private AudioRecorder audioRecorder;
    private GroqApiClient groqApiClient;
    private Handler handler;
    private boolean isRecording = false;
    private boolean isProcessing = false;
    private android.graphics.drawable.AnimationDrawable processingAnimation;
    private File audioFile;
    private TranscriptionRepository transcriptionRepository;

    private float initialX;
    private float initialY;
    private float initialTouchX;
    private float initialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize audio recorder and API client
        audioRecorder = new AudioRecorder(this);
        groqApiClient = new GroqApiClient();
        handler = new Handler(Looper.getMainLooper());

        // Initialize repository
        transcriptionRepository = new TranscriptionRepository(getApplication());

        // Create notification channel for Android 8.0+
        createNotificationChannel();

        // Start as a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+ (API 29+)
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else { // Android 9 and below (API 28 and below)
            startForeground(NOTIFICATION_ID, createNotification());
        }

        // Log that service has started
        Log.d(TAG, "OverlayService started successfully");

        // Initialize the overlay view
        initializeOverlayView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the overlay view
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }

        // Stop recording if active
        if (isRecording && audioRecorder != null) {
            audioRecorder.stopRecording();
            isRecording = false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Transbord Overlay Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps the overlay button active");

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
                .setContentTitle("Transbord")
                .setContentText("Overlay mode is active")
                .setSmallIcon(R.drawable.ic_mic)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void initializeOverlayView() {
        try {
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // Create a ContextThemeWrapper with AppCompat theme
            Context contextThemeWrapper = new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat);

            // Inflate the overlay view with the themed context
            LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
            overlayView = inflater.inflate(R.layout.layout_floating_button, null);

            // Find the FAB in the inflated view
            fabOverlayRecord = overlayView.findViewById(R.id.fab_overlay_record);

            if (fabOverlayRecord == null) {
                Log.e(TAG, "Failed to find fab_overlay_record in the inflated layout");
                Toast.makeText(this, "Error: Could not create overlay button", Toast.LENGTH_LONG).show();
                return;
            }

            // Make the button more visible
            fabOverlayRecord.setAlpha(1.0f);
            fabOverlayRecord.setSize(FloatingActionButton.SIZE_NORMAL);
            fabOverlayRecord.setCompatElevation(16f);

            // Set up window parameters
            int overlayType;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                overlayType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    overlayType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                    PixelFormat.TRANSLUCENT
            );

            // Initial position - more visible position
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = 24;
            params.y = 200;
            params.alpha = 1.0f;

            // Add the view to the window
            windowManager.addView(overlayView, params);

            // Log success
            Log.d(TAG, "Overlay view added successfully");

            // Show a toast to confirm the overlay is active
            Toast.makeText(this, "Overlay button is now active", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to add overlay view", e);
            Toast.makeText(this, "Failed to create overlay: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return; // Exit the method to prevent NullPointerException
        }

        // Set up both click and touch listeners for the FAB
        if (fabOverlayRecord != null) {
            // Set up click listener
            fabOverlayRecord.setOnClickListener(v -> {
                Log.d(TAG, "FAB clicked");
                handleRecordButtonClick();
            });

            // Set up touch listener for dragging directly on the FAB
            fabOverlayRecord.setOnTouchListener((v, event) -> {
                WindowManager.LayoutParams updatedParams = (WindowManager.LayoutParams) overlayView.getLayoutParams();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Provide haptic feedback on touch
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                        }

                        // Record initial positions
                        initialX = updatedParams.x;
                        initialY = updatedParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false; // Allow click events to be detected

                    case MotionEvent.ACTION_MOVE:
                        // Calculate how far we've moved
                        float deltaX = event.getRawX() - initialTouchX;
                        float deltaY = event.getRawY() - initialTouchY;

                        // Only start dragging after a threshold to distinguish from clicks
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            // Update the position
                            updatedParams.x = (int) (initialX + deltaX);
                            updatedParams.y = (int) (initialY + deltaY);

                            try {
                                windowManager.updateViewLayout(overlayView, updatedParams);
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating overlay position", e);
                            }

                            // Visual feedback during drag
                            fabOverlayRecord.setAlpha(0.8f);
                            return true; // Consume the event
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Reset alpha
                        fabOverlayRecord.setAlpha(1.0f);

                        // If we didn't move much, it's a click
                        if (Math.abs(event.getRawX() - initialTouchX) < 10 &&
                                Math.abs(event.getRawY() - initialTouchY) < 10) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            });
        } else {
            Log.e(TAG, "fabOverlayRecord is null, cannot set listeners");
            return; // Exit the method to prevent further NullPointerException
        }
    }

    private void handleRecordButtonClick() {
        // Provide haptic feedback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        } else {
            fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }

        // Show visual feedback
        fabOverlayRecord.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                .withEndAction(() -> fabOverlayRecord.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();

        // Show toast message
        Toast.makeText(this, !isRecording ? "Starting recording..." : "Stopping recording...", Toast.LENGTH_SHORT).show();

        if (!isRecording) {
            startRecording();
        } else {
            stopRecordingAndTranscribe();
        }

        Log.d(TAG, "Record button clicked, recording state: " + isRecording);
    }

    private void startRecording() {
        // Start recording
        if (audioRecorder.startRecording()) {
            isRecording = true;

            // Update UI
            fabOverlayRecord.setImageResource(R.drawable.ic_record_new);
            fabOverlayRecord.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.recording, null)));
            // Add a pulsating animation
            fabOverlayRecord.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        if (isRecording) { // Check if still recording
                            fabOverlayRecord.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(500)
                                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                    .withEndAction(() -> {
                                        if (isRecording) { // Check if still recording before repeating
                                            startPulseAnimation();
                                        }
                                    })
                                    .start();
                        }
                    })
                    .start();

            // Provide haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndTranscribe() {
        if (isRecording) {
            // Stop recording
            audioFile = audioRecorder.stopRecording();
            isRecording = false;
            isProcessing = true;

            // Update UI to show processing state
            fabOverlayRecord.setImageResource(R.drawable.processing_frame2);
            fabOverlayRecord.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.processing, null)));

            // Start processing animation
            startProcessingAnimation();

            // Provide haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.REJECT);
            } else {
                fabOverlayRecord.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            if (audioFile != null && audioFile.exists()) {
                // Show processing toast
                Toast.makeText(this, R.string.processing_transcription, Toast.LENGTH_SHORT).show();

                // Transcribe audio
                transcribeAudio(audioFile);
            } else {
                // Reset UI if failed
                isProcessing = false;
                resetButtonToDefault();
                Toast.makeText(this, "Failed to save recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void transcribeAudio(File audioFile) {
        Log.d(TAG, "Transcribing audio file: " + audioFile.getAbsolutePath());

        // Call Groq API to transcribe audio
        groqApiClient.transcribeAudio(audioFile, "en", new GroqApiClient.TranscriptionCallback() {
            @Override
            public void onSuccess(TranscriptionResponse response) {
                handler.post(() -> {
                    // Stop processing state
                    isProcessing = false;
                    resetButtonToDefault();

                    // Get transcription text
                    String transcriptionText = response.getText();

                    // Try to insert text using accessibility service
                    boolean inserted = false;
                    if (TransbordAccessibilityService.isRunning()) {
                        inserted = TransbordAccessibilityService.getInstance().injectText(transcriptionText);
                        if (inserted) {
                            Toast.makeText(OverlayService.this, R.string.text_inserted, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OverlayService.this, R.string.text_insertion_failed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Just show a toast without opening settings to avoid leaving overlay mode
                        Toast.makeText(OverlayService.this, R.string.accessibility_permission_required, Toast.LENGTH_LONG).show();
                    }

                    // Save transcription in the app
                    saveTranscription(transcriptionText, audioFile.getAbsolutePath());
                });
            }

            @Override
            public void onFailure(Exception e) {
                handler.post(() -> {
                    // Stop processing state
                    isProcessing = false;
                    resetButtonToDefault();

                    Log.e(TAG, "Transcription failed", e);
                    Toast.makeText(OverlayService.this, "Transcription failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveTranscription(String transcriptionText, String audioFilePath) {
        // Create a new transcription object
        long timestamp = System.currentTimeMillis();
        String title = Transcription.generateTitleFromText(transcriptionText);

        // Create transcription object
        Transcription transcription = new Transcription(
                transcriptionText,
                audioFilePath,
                timestamp,
                title
        );

        // Save directly to database
        transcriptionRepository.insert(transcription, id -> {
            handler.post(() -> {
                // Show success message
                Toast.makeText(OverlayService.this, R.string.transcription_saved_overlay, Toast.LENGTH_SHORT).show();
            });
        });
    }

    // Removed generateTitleFromText method as we're now using the one from Transcription class

    /**
     * Starts a pulsating animation for the recording button
     */
    private void startPulseAnimation() {
        if (isRecording && fabOverlayRecord != null) {
            fabOverlayRecord.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        if (isRecording) { // Check if still recording
                            fabOverlayRecord.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(500)
                                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                    .withEndAction(() -> {
                                        if (isRecording) { // Check if still recording before repeating
                                            startPulseAnimation();
                                        }
                                    })
                                    .start();
                        }
                    })
                    .start();
        }
    }

    /**
     * Starts the processing animation
     */
    private void startProcessingAnimation() {
        if (fabOverlayRecord != null) {
            // Create a manual animation that cycles through the processing frames
            handler.post(new Runnable() {
                private int currentFrame = 0;
                private final int[] frames = {
                        R.drawable.processing_frame1,
                        R.drawable.processing_frame2,
                        R.drawable.processing_frame3
                };

                @Override
                public void run() {
                    if (isProcessing && fabOverlayRecord != null) {
                        // Update the image resource to the next frame
                        fabOverlayRecord.setImageResource(frames[currentFrame]);

                        // Move to the next frame, looping back to the start if needed
                        currentFrame = (currentFrame + 1) % frames.length;

                        // Schedule the next frame update
                        handler.postDelayed(this, 300);
                    }
                }
            });
        }
    }

    /**
     * Resets the button to its default state
     */
    private void resetButtonToDefault() {
        if (fabOverlayRecord != null) {
            fabOverlayRecord.setImageResource(R.drawable.ic_mic);
            fabOverlayRecord.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary, null)));
            fabOverlayRecord.clearAnimation();
        }
    }
}
