package com.example.transbord;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.transbord.services.HotwordService;
import com.example.transbord.utils.VoiceCommandManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class VoiceCommandSettingsActivity extends AppCompatActivity {

    private VoiceCommandManager voiceCommandManager;
    private SwitchMaterial switchEnableVoiceCommands;
    private TextView tvCurrentHotword;
    private MaterialButton btnChangeHotword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voice_command_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize voice command manager
        voiceCommandManager = new VoiceCommandManager(this);

        // Initialize views
        switchEnableVoiceCommands = findViewById(R.id.switch_enable_voice_commands);
        tvCurrentHotword = findViewById(R.id.tv_current_hotword);
        btnChangeHotword = findViewById(R.id.btn_change_hotword);

        // Set up UI
        updateUI();

        // Set up listeners
        setupListeners();
    }

    private void updateUI() {
        // Update switch state
        switchEnableVoiceCommands.setChecked(voiceCommandManager.isCommandsEnabled());

        // Update current hotword text
        tvCurrentHotword.setText(getString(R.string.current_hotword, voiceCommandManager.getHotword()));
    }

    private void setupListeners() {
        // Enable/disable voice commands
        switchEnableVoiceCommands.setOnCheckedChangeListener((buttonView, isChecked) -> {
            voiceCommandManager.setCommandsEnabled(isChecked);

            if (isChecked) {
                // Start hotword service
                Intent intent = new Intent(this, HotwordService.class);
                startService(intent);
            } else {
                // Stop hotword service
                Intent intent = new Intent(this, HotwordService.class);
                stopService(intent);
            }
        });

        // Change hotword button
        btnChangeHotword.setOnClickListener(v -> showChangeHotwordDialog());
    }

    private void showChangeHotwordDialog() {
        // Create dialog view with EditText
        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setHint(getString(R.string.enter_new_hotword));
        TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());
        editText.setText(voiceCommandManager.getHotword());
        textInputLayout.addView(editText);
        textInputLayout.setPadding(64, 32, 64, 0);

        // Create and show dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_hotword)
                .setView(textInputLayout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String newHotword = editText.getText() != null ? 
                            editText.getText().toString().trim() : "";
                    
                    if (!newHotword.isEmpty()) {
                        voiceCommandManager.setHotword(newHotword);
                        updateUI();
                        
                        // Restart hotword service if enabled
                        if (voiceCommandManager.isCommandsEnabled()) {
                            Intent intent = new Intent(this, HotwordService.class);
                            stopService(intent);
                            startService(intent);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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
