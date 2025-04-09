package com.example.transbord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transbord.adapters.TranscriptionAdapter;
import com.example.transbord.data.Transcription;
import com.example.transbord.data.TranscriptionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class SavedTranscriptionsActivity extends AppCompatActivity implements 
        TranscriptionAdapter.OnTranscriptionClickListener, 
        TranscriptionAdapter.OnTranscriptionOptionsClickListener {
    
    private TranscriptionViewModel transcriptionViewModel;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TranscriptionAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_transcriptions);
        
        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        FloatingActionButton fabNewRecording = findViewById(R.id.fab_new_recording);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        
        // Set up adapter
        adapter = new TranscriptionAdapter(this, this);
        recyclerView.setAdapter(adapter);
        
        // Set up ViewModel
        transcriptionViewModel = new ViewModelProvider(this).get(TranscriptionViewModel.class);
        
        // Observe transcriptions
        transcriptionViewModel.getAllTranscriptions().observe(this, transcriptions -> {
            adapter.submitList(transcriptions);
            
            // Show empty view if no transcriptions
            if (transcriptions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            }
        });
        
        // Set up FAB click listener
        fabNewRecording.setOnClickListener(v -> {
            // Navigate to MainActivity to start a new recording
            Intent intent = new Intent(SavedTranscriptionsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    public void onTranscriptionClick(Transcription transcription) {
        // Navigate to TranscriptionActivity to view the transcription
        Intent intent = new Intent(SavedTranscriptionsActivity.this, TranscriptionActivity.class);
        intent.putExtra(TranscriptionActivity.EXTRA_TRANSCRIPTION_ID, transcription.getId());
        intent.putExtra(TranscriptionActivity.EXTRA_TRANSCRIPTION_TEXT, transcription.getText());
        intent.putExtra(TranscriptionActivity.EXTRA_AUDIO_FILE_PATH, transcription.getAudioFilePath());
        startActivity(intent);
    }
    
    @Override
    public void onOptionsClick(Transcription transcription, View anchorView) {
        // Show popup menu
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.inflate(R.menu.menu_transcription_options);
        
        // Check if audio file exists
        boolean audioFileExists = false;
        if (transcription.getAudioFilePath() != null) {
            File audioFile = new File(transcription.getAudioFilePath());
            audioFileExists = audioFile.exists();
        }
        
        // Disable play option if audio file doesn't exist
        popupMenu.getMenu().findItem(R.id.action_play).setVisible(audioFileExists);
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.action_play) {
                // Navigate to TranscriptionActivity to play the audio
                onTranscriptionClick(transcription);
                return true;
            } else if (itemId == R.id.action_share) {
                // Share transcription text
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, transcription.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, transcription.getText());
                startActivity(Intent.createChooser(shareIntent, "Share Transcription"));
                return true;
            } else if (itemId == R.id.action_delete) {
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(transcription);
                return true;
            }
            
            return false;
        });
        
        popupMenu.show();
    }
    
    private void showDeleteConfirmationDialog(Transcription transcription) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Delete audio file if it exists
                    if (transcription.getAudioFilePath() != null) {
                        File audioFile = new File(transcription.getAudioFilePath());
                        if (audioFile.exists()) {
                            audioFile.delete();
                        }
                    }
                    
                    // Delete transcription from database
                    transcriptionViewModel.delete(transcription);
                    Toast.makeText(this, R.string.transcription_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
