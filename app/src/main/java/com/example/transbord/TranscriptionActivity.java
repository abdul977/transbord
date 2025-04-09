package com.example.transbord;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.transbord.data.Transcription;
import com.example.transbord.data.TranscriptionRepository;
import com.example.transbord.data.TranscriptionViewModel;
import com.example.transbord.utils.TemplateManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TranscriptionActivity extends AppCompatActivity {

    private static final String TAG = "TranscriptionActivity";

    public static final String EXTRA_TRANSCRIPTION_TEXT = "extra_transcription_text";
    public static final String EXTRA_AUDIO_FILE_PATH = "extra_audio_file_path";
    public static final String EXTRA_TRANSCRIPTION_ID = "extra_transcription_id";

    private TextView tvTranscription;
    private MaterialButton btnPlay;
    private MaterialButton btnEnhance;
    private MaterialButton btnDelete;
    private CardView cardTranscription;

    private String transcriptionText;
    private String audioFilePath;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private TranscriptionViewModel transcriptionViewModel;
    private long transcriptionId = -1; // -1 means new transcription
    private TemplateManager templateManager;
    private SharedPreferences templatePrefs;

    private ActivityResultLauncher<Intent> reasoningLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transcription);

        // Initialize views
        tvTranscription = findViewById(R.id.tv_transcription);
        btnPlay = findViewById(R.id.btn_play);
        btnEnhance = findViewById(R.id.btn_enhance);
        btnDelete = findViewById(R.id.btn_delete);
        cardTranscription = findViewById(R.id.card_transcription);

        // Initialize reasoning launcher
        reasoningLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String processedText = result.getData().getStringExtra(ReasoningActivity.EXTRA_RESULT);
                        if (processedText != null && !processedText.isEmpty()) {
                            // Update the transcription text
                            transcriptionText = processedText;
                            tvTranscription.setText(transcriptionText);
                            Toast.makeText(this, "Text updated with AI processing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Initialize ViewModel
        transcriptionViewModel = new ViewModelProvider(this).get(TranscriptionViewModel.class);

        // Initialize template manager
        templateManager = new TemplateManager(this);
        templatePrefs = getSharedPreferences("template_settings", MODE_PRIVATE);
        boolean autoFormatEnabled = templatePrefs.getBoolean("auto_format_enabled", false);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            transcriptionId = intent.getLongExtra(EXTRA_TRANSCRIPTION_ID, -1);
            transcriptionText = intent.getStringExtra(EXTRA_TRANSCRIPTION_TEXT);
            audioFilePath = intent.getStringExtra(EXTRA_AUDIO_FILE_PATH);
            boolean autoSave = intent.getBooleanExtra("auto_save", false);

            if (transcriptionId != -1) {
                // Load existing transcription
                transcriptionViewModel.getTranscriptionById(transcriptionId, transcription -> {
                    if (transcription != null) {
                        transcriptionText = transcription.getText();
                        audioFilePath = transcription.getAudioFilePath();
                        tvTranscription.setText(transcriptionText);

                        // Existing transcription loaded
                    }
                });
            } else if (transcriptionText != null) {
                tvTranscription.setText(transcriptionText);

                // Apply auto-formatting if enabled
                try {
                    if (templatePrefs.getBoolean("auto_format_enabled", false)) {
                        Log.d(TAG, "Auto-formatting enabled, detecting template type");
                        int templateType = templateManager.detectTemplateType(transcriptionText);
                        Log.d(TAG, "Detected template type: " + templateType);
                        if (templateType > 0) {
                            String formattedText = templateManager.formatText(transcriptionText, templateType);
                            tvTranscription.setText(formattedText);
                            transcriptionText = formattedText;
                            Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "No template detected for auto-formatting");
                        }
                    } else {
                        Log.d(TAG, "Auto-formatting is disabled");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error applying auto-formatting: " + e.getMessage());
                }

                // Auto-save if requested (from overlay service)
                if (autoSave) {
                    String title = Transcription.generateTitleFromText(transcriptionText);
                    saveTranscription(title);
                    Toast.makeText(this, R.string.transcription_saved_and_inserted, Toast.LENGTH_SHORT).show();
                }

                // Add back button press listener to auto-save
                findViewById(android.R.id.content).setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                        // Auto-save when back button is pressed
                        if (transcriptionId == -1 && transcriptionText != null && !transcriptionText.isEmpty()) {
                            String title = Transcription.generateTitleFromText(transcriptionText);
                            saveTranscription(title);
                        }
                        return false; // Allow normal back button behavior
                    }
                    return false;
                });
            }
        }

        // Set up click listeners
        setupClickListeners();

        // Set up long click listener for the transcription text
        setupTextSelectionHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void setupClickListeners() {
        // Play button click listener
        btnPlay.setOnClickListener(v -> {
            if (audioFilePath != null) {
                if (!isPlaying) {
                    playAudio();
                } else {
                    stopAudio();
                }
            } else {
                Toast.makeText(this, "No audio file available", Toast.LENGTH_SHORT).show();
            }
        });

        // Enhance with AI button click listener
        btnEnhance.setOnClickListener(v -> {
            if (transcriptionText != null && !transcriptionText.isEmpty()) {
                // Launch ReasoningActivity
                Intent intent = new Intent(TranscriptionActivity.this, ReasoningActivity.class);
                intent.putExtra(ReasoningActivity.EXTRA_TEXT, transcriptionText);
                reasoningLauncher.launch(intent);
            } else {
                Toast.makeText(this, "No text to enhance", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button click listener
        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void setupTextSelectionHint() {
        // Show a hint toast when the user first clicks on the text
        tvTranscription.setOnClickListener(v -> {
            Toast.makeText(this, R.string.text_copy_hint, Toast.LENGTH_SHORT).show();
            // Remove the click listener after showing the hint once
            tvTranscription.setOnClickListener(null);
        });

        // Make the card also show the hint
        cardTranscription.setOnClickListener(v -> {
            Toast.makeText(this, R.string.text_copy_hint, Toast.LENGTH_SHORT).show();
            // Remove the click listener after showing the hint once
            cardTranscription.setOnClickListener(null);
        });
    }

    private void playAudio() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(mp -> {
                    stopAudio();
                });
            }
            mediaPlayer.start();
            isPlaying = true;
            btnPlay.setText(R.string.done);
            Toast.makeText(this, "Playing audio...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error playing audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        btnPlay.setText(R.string.play_audio);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void saveTranscription(String title) {
        if (transcriptionText == null || transcriptionText.isEmpty()) {
            Toast.makeText(this, "No transcription to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new transcription object
        Transcription transcription = new Transcription(
                transcriptionText,
                audioFilePath,
                new Date().getTime(),
                title
        );

        if (transcriptionId != -1) {
            // Update existing transcription
            transcription.setId(transcriptionId);
            transcriptionViewModel.update(transcription);
        } else {
            // Save new transcription
            transcriptionViewModel.insert(transcription, id -> {
                runOnUiThread(() -> {
                    Toast.makeText(TranscriptionActivity.this, R.string.transcription_saved, Toast.LENGTH_SHORT).show();
                });
            });
        }

        // Navigate to SavedTranscriptionsActivity
        Intent intent = new Intent(TranscriptionActivity.this, SavedTranscriptionsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Delete audio file if it exists
                    if (audioFilePath != null) {
                        File audioFile = new File(audioFilePath);
                        if (audioFile.exists()) {
                            audioFile.delete();
                        }
                    }

                    // Delete transcription from database if it exists
                    if (transcriptionId != -1) {
                        transcriptionViewModel.deleteById(transcriptionId);
                    }

                    Toast.makeText(this, R.string.transcription_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transcription, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_format_email) {
            String formatted = templateManager.formatAsEmail(transcriptionText);
            tvTranscription.setText(formatted);
            transcriptionText = formatted;
            Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_format_list) {
            String formatted = templateManager.formatAsList(transcriptionText);
            tvTranscription.setText(formatted);
            transcriptionText = formatted;
            Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_format_notes) {
            String formatted = templateManager.formatText(transcriptionText, TemplateManager.TEMPLATE_NOTES);
            tvTranscription.setText(formatted);
            transcriptionText = formatted;
            Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_format_meeting) {
            String formatted = templateManager.formatAsMeetingNotes(transcriptionText);
            tvTranscription.setText(formatted);
            transcriptionText = formatted;
            Toast.makeText(this, R.string.template_applied, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
