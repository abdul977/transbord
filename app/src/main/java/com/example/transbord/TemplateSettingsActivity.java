package com.example.transbord;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transbord.adapters.CustomTemplateAdapter;
import com.example.transbord.utils.TemplateManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class TemplateSettingsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "template_settings";
    private static final String KEY_AUTO_FORMAT = "auto_format_enabled";

    private TemplateManager templateManager;
    private SwitchMaterial switchAutoFormat;
    private RecyclerView rvCustomTemplates;
    private MaterialButton btnAddTemplate;
    private CustomTemplateAdapter adapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_template_settings);

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

        // Initialize preferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize template manager
        templateManager = new TemplateManager(this);

        // Initialize views
        switchAutoFormat = findViewById(R.id.switch_auto_format);
        rvCustomTemplates = findViewById(R.id.rv_custom_templates);
        btnAddTemplate = findViewById(R.id.btn_add_template);

        // Set up RecyclerView
        rvCustomTemplates.setLayoutManager(new LinearLayoutManager(this));
        List<TemplateManager.CustomTemplate> customTemplates = templateManager.getCustomTemplates();
        adapter = new CustomTemplateAdapter(customTemplates, this::showEditTemplateDialog, this::deleteTemplate);
        rvCustomTemplates.setAdapter(adapter);

        // Set up switch state
        switchAutoFormat.setChecked(preferences.getBoolean(KEY_AUTO_FORMAT, false));

        // Set up listeners
        setupListeners();
    }

    private void setupListeners() {
        // Auto-format switch
        switchAutoFormat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_AUTO_FORMAT, isChecked).apply();
        });

        // Add template button
        btnAddTemplate.setOnClickListener(v -> showAddTemplateDialog());
    }

    private void showAddTemplateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_template, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_template_name);
        TextInputEditText etPattern = dialogView.findViewById(R.id.et_template_pattern);
        TextInputEditText etFormat = dialogView.findViewById(R.id.et_template_format);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_custom_template)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String pattern = etPattern.getText() != null ? etPattern.getText().toString().trim() : "";
                    String format = etFormat.getText() != null ? etFormat.getText().toString().trim() : "";

                    if (!name.isEmpty() && !pattern.isEmpty() && !format.isEmpty()) {
                        templateManager.addCustomTemplate(name, pattern, format);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, R.string.template_added, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditTemplateDialog(int position, TemplateManager.CustomTemplate template) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_template, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_template_name);
        TextInputEditText etPattern = dialogView.findViewById(R.id.et_template_pattern);
        TextInputEditText etFormat = dialogView.findViewById(R.id.et_template_format);

        // Set existing values
        etName.setText(template.getName());
        etPattern.setText(template.getPattern());
        etFormat.setText(template.getFormat());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_template)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String pattern = etPattern.getText() != null ? etPattern.getText().toString().trim() : "";
                    String format = etFormat.getText() != null ? etFormat.getText().toString().trim() : "";

                    if (!name.isEmpty() && !pattern.isEmpty() && !format.isEmpty()) {
                        // Remove old template and add updated one
                        templateManager.removeCustomTemplate(position);
                        templateManager.addCustomTemplate(name, pattern, format);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, R.string.template_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteTemplate(int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_template)
                .setMessage("Are you sure you want to delete this template?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    templateManager.removeCustomTemplate(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, R.string.template_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null)
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
