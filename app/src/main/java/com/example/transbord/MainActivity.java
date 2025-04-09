package com.example.transbord;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.transbord.api.GroqApiClient;
import com.example.transbord.api.TranscriptionResponse;
import com.example.transbord.services.OverlayService;
import com.example.transbord.utils.AccessibilityUtil;
import com.example.transbord.utils.AudioRecorder;
import com.example.transbord.utils.PermissionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 101;

    private FloatingActionButton fabRecord;
    private CardView cardStatus;
    private TextView tvStatus;
    private TextView tvTranscription;
    private ImageView ivSettings;

    private AudioRecorder audioRecorder;
    private GroqApiClient groqApiClient;
    private Handler handler;
    private boolean isRecording = false;
    private File audioFile;

    private ActivityResultLauncher<Intent> overlayPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        fabRecord = findViewById(R.id.fab_record);
        cardStatus = findViewById(R.id.card_status);
        tvStatus = findViewById(R.id.tv_status);
        tvTranscription = findViewById(R.id.tv_transcription);
        ivSettings = findViewById(R.id.iv_settings);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize audio recorder and API client
        audioRecorder = new AudioRecorder(this);
        groqApiClient = new GroqApiClient();
        handler = new Handler(Looper.getMainLooper());

        // Initialize overlay permission launcher
        overlayPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (PermissionManager.canDrawOverlays(this)) {
                        startOverlayService();
                    } else {
                        Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Check permissions
        if (!PermissionManager.checkPermissions(this)) {
            PermissionManager.requestPermissions(this);
        }

        // Set up click listeners
        setupClickListeners();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions are required to use this app", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setupClickListeners() {
        // Record button click listener
        fabRecord.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecordingAndTranscribe();
            }
        });

        // Long press to activate overlay
        fabRecord.setOnLongClickListener(v -> {
            requestOverlayPermissionAndStartService();
            return true;
        });

        // Settings button click listener
        ivSettings.setOnClickListener(v -> {
            // Show options menu
            openOptionsMenu();
        });
    }

    private void startRecording() {
        if (!PermissionManager.checkPermissions(this)) {
            PermissionManager.requestPermissions(this);
            return;
        }

        // Start recording
        if (audioRecorder.startRecording()) {
            isRecording = true;

            // Update UI
            cardStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(R.string.listening);
            tvTranscription.setText("");
            fabRecord.setImageResource(R.drawable.ic_stop);

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

            // Update UI
            tvStatus.setText(R.string.processing);
            fabRecord.setImageResource(R.drawable.ic_mic);

            if (audioFile != null && audioFile.exists()) {
                // Transcribe audio
                transcribeAudio(audioFile);
            } else {
                Toast.makeText(this, "Failed to save recording", Toast.LENGTH_SHORT).show();
                cardStatus.setVisibility(View.GONE);
            }
        }
    }

    private void transcribeAudio(File audioFile) {
        Log.d(TAG, "Transcribing audio file: " + audioFile.getAbsolutePath());

        // Show processing state
        tvStatus.setText(R.string.processing);

        // Call Groq API to transcribe audio
        groqApiClient.transcribeAudio(audioFile, "en", new GroqApiClient.TranscriptionCallback() {
            @Override
            public void onSuccess(TranscriptionResponse response) {
                handler.post(() -> {
                    // Update UI with transcription
                    tvStatus.setText(R.string.done);
                    String transcriptionText = response.getText();
                    tvTranscription.setText(transcriptionText);

                    // Launch TranscriptionActivity
                    Intent intent = new Intent(MainActivity.this, TranscriptionActivity.class);
                    intent.putExtra(TranscriptionActivity.EXTRA_TRANSCRIPTION_TEXT, transcriptionText);
                    intent.putExtra(TranscriptionActivity.EXTRA_AUDIO_FILE_PATH, audioFile.getAbsolutePath());
                    startActivity(intent);

                    // Reset UI
                    cardStatus.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(Exception e) {
                handler.post(() -> {
                    Log.e(TAG, "Transcription failed", e);
                    Toast.makeText(MainActivity.this, "Transcription failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvStatus.setText(R.string.done);
                    tvTranscription.setText("Transcription failed. Please try again.");
                });
            }
        });
    }

    private void requestOverlayPermissionAndStartService() {
        // Check overlay permission
        if (!PermissionManager.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.overlay_permission_required, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
            return;
        }

        // Check accessibility permission
        if (!AccessibilityUtil.isAccessibilityServiceEnabled(this)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.accessibility_permission_required)
                    .setMessage(R.string.accessibility_service_description)
                    .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                        AccessibilityUtil.openAccessibilitySettings(this);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // Start overlay service anyway
                        startOverlayService();
                    })
                    .show();
        } else {
            startOverlayService();
        }
    }

    private void startOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Overlay mode activated", Toast.LENGTH_SHORT).show();
        // Minimize the app
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_saved) {
            // Navigate to SavedTranscriptionsActivity
            Intent intent = new Intent(MainActivity.this, SavedTranscriptionsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            // Navigate to AboutActivity
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}