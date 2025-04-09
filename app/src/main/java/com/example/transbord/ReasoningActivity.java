package com.example.transbord;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.transbord.api.ReasoningApiClient;
import com.example.transbord.api.ReasoningResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ReasoningActivity extends AppCompatActivity {

    private static final String TAG = "ReasoningActivity";
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_RESULT = "extra_result";

    private TextView tvOriginalText;
    private TextInputLayout tilPrompt;
    private TextInputEditText etPrompt;
    private MaterialButton btnProcess;
    private MaterialButton btnUseProcessed;
    private CardView cardProcessedText;
    private TextView tvProcessedText;
    private CardView cardReasoning;
    private TextView tvReasoning;
    private ProgressBar progressBar;

    private String originalText;
    private String processedText;
    private ReasoningApiClient reasoningApiClient;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reasoning);

        // Initialize views
        tvOriginalText = findViewById(R.id.tv_original_text);
        tilPrompt = findViewById(R.id.til_prompt);
        etPrompt = findViewById(R.id.et_prompt);
        btnProcess = findViewById(R.id.btn_process);
        btnUseProcessed = findViewById(R.id.btn_use_processed);
        cardProcessedText = findViewById(R.id.card_processed_text);
        tvProcessedText = findViewById(R.id.tv_processed_text);
        cardReasoning = findViewById(R.id.card_reasoning);
        tvReasoning = findViewById(R.id.tv_reasoning);
        progressBar = findViewById(R.id.progress_bar);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize API client and handler
        reasoningApiClient = new ReasoningApiClient();
        handler = new Handler(Looper.getMainLooper());

        // Get original text from intent
        if (getIntent() != null && getIntent().hasExtra(EXTRA_TEXT)) {
            originalText = getIntent().getStringExtra(EXTRA_TEXT);
            tvOriginalText.setText(originalText);
        } else {
            Toast.makeText(this, "No text provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up click listeners
        setupClickListeners();

        // Set up text selection hints
        setupTextSelectionHints();
    }

    private void setupClickListeners() {
        // Process button click listener
        btnProcess.setOnClickListener(v -> {
            String prompt = etPrompt.getText() != null ? etPrompt.getText().toString() : "";
            if (TextUtils.isEmpty(prompt)) {
                tilPrompt.setError("Please enter an instruction");
                return;
            }

            tilPrompt.setError(null);
            processTextWithAI(prompt);
        });

        // Use processed text button click listener
        btnUseProcessed.setOnClickListener(v -> {
            if (processedText != null) {
                // Return the processed text to the calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_RESULT, processedText);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void processTextWithAI(String prompt) {
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        cardProcessedText.setVisibility(View.GONE);
        cardReasoning.setVisibility(View.GONE);
        btnUseProcessed.setVisibility(View.GONE);
        btnProcess.setEnabled(false);

        // Call the API
        reasoningApiClient.processText(originalText, prompt, new ReasoningApiClient.ReasoningCallback() {
            @Override
            public void onSuccess(ReasoningResponse response) {
                handler.post(() -> {
                    // Hide loading state
                    progressBar.setVisibility(View.GONE);
                    btnProcess.setEnabled(true);

                    // Get the processed text and reasoning
                    processedText = response.getContent();
                    String reasoning = response.getReasoning();

                    // Update UI
                    if (!TextUtils.isEmpty(processedText)) {
                        cardProcessedText.setVisibility(View.VISIBLE);
                        tvProcessedText.setText(processedText);
                        btnUseProcessed.setVisibility(View.VISIBLE);
                    }

                    if (!TextUtils.isEmpty(reasoning)) {
                        cardReasoning.setVisibility(View.VISIBLE);
                        tvReasoning.setText(reasoning);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                handler.post(() -> {
                    // Hide loading state
                    progressBar.setVisibility(View.GONE);
                    btnProcess.setEnabled(true);

                    // Show error message
                    Log.e(TAG, "AI processing failed", e);
                    Toast.makeText(ReasoningActivity.this,
                            getString(R.string.ai_processing_failed) + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupTextSelectionHints() {
        // Show a hint toast when the user first clicks on the original text
        tvOriginalText.setOnClickListener(v -> {
            Toast.makeText(this, R.string.text_copy_hint, Toast.LENGTH_SHORT).show();
            // Remove the click listener after showing the hint once
            tvOriginalText.setOnClickListener(null);
        });

        // Show a hint toast when the user first clicks on the processed text
        tvProcessedText.setOnClickListener(v -> {
            Toast.makeText(this, R.string.text_copy_hint, Toast.LENGTH_SHORT).show();
            // Remove the click listener after showing the hint once
            tvProcessedText.setOnClickListener(null);
        });

        // Show a hint toast when the user first clicks on the reasoning text
        tvReasoning.setOnClickListener(v -> {
            Toast.makeText(this, R.string.text_copy_hint, Toast.LENGTH_SHORT).show();
            // Remove the click listener after showing the hint once
            tvReasoning.setOnClickListener(null);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
