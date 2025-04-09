package com.example.transbord.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TranscriptionViewModel extends AndroidViewModel {
    
    private final TranscriptionRepository repository;
    private final LiveData<List<Transcription>> allTranscriptions;
    
    public TranscriptionViewModel(@NonNull Application application) {
        super(application);
        repository = new TranscriptionRepository(application);
        allTranscriptions = repository.getAllTranscriptions();
    }
    
    public LiveData<List<Transcription>> getAllTranscriptions() {
        return allTranscriptions;
    }
    
    public LiveData<List<Transcription>> searchTranscriptions(String query) {
        return repository.searchTranscriptions(query);
    }
    
    public void insert(Transcription transcription, TranscriptionRepository.OnTranscriptionSavedListener listener) {
        repository.insert(transcription, listener);
    }
    
    public void update(Transcription transcription) {
        repository.update(transcription);
    }
    
    public void delete(Transcription transcription) {
        repository.delete(transcription);
    }
    
    public void deleteById(long id) {
        repository.deleteById(id);
    }
    
    public void deleteAllTranscriptions() {
        repository.deleteAllTranscriptions();
    }
    
    public void getTranscriptionById(long id, TranscriptionRepository.OnTranscriptionLoadedListener listener) {
        repository.getTranscriptionById(id, listener);
    }
}
