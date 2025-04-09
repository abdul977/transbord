package com.example.transbord.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranscriptionRepository {
    
    private final TranscriptionDao transcriptionDao;
    private final LiveData<List<Transcription>> allTranscriptions;
    private final ExecutorService executorService;
    
    public TranscriptionRepository(Application application) {
        TransbordDatabase database = TransbordDatabase.getInstance(application);
        transcriptionDao = database.transcriptionDao();
        allTranscriptions = transcriptionDao.getAllTranscriptions();
        executorService = Executors.newFixedThreadPool(4);
    }
    
    public LiveData<List<Transcription>> getAllTranscriptions() {
        return allTranscriptions;
    }
    
    public LiveData<List<Transcription>> searchTranscriptions(String query) {
        return transcriptionDao.searchTranscriptions(query);
    }
    
    public void insert(Transcription transcription, OnTranscriptionSavedListener listener) {
        executorService.execute(() -> {
            long id = transcriptionDao.insert(transcription);
            if (listener != null) {
                listener.onTranscriptionSaved(id);
            }
        });
    }
    
    public void update(Transcription transcription) {
        executorService.execute(() -> transcriptionDao.update(transcription));
    }
    
    public void delete(Transcription transcription) {
        executorService.execute(() -> transcriptionDao.delete(transcription));
    }
    
    public void deleteById(long id) {
        executorService.execute(() -> transcriptionDao.deleteById(id));
    }
    
    public void deleteAllTranscriptions() {
        executorService.execute(transcriptionDao::deleteAllTranscriptions);
    }
    
    public void getTranscriptionById(long id, OnTranscriptionLoadedListener listener) {
        executorService.execute(() -> {
            Transcription transcription = transcriptionDao.getTranscriptionById(id);
            if (listener != null) {
                listener.onTranscriptionLoaded(transcription);
            }
        });
    }
    
    public interface OnTranscriptionSavedListener {
        void onTranscriptionSaved(long id);
    }
    
    public interface OnTranscriptionLoadedListener {
        void onTranscriptionLoaded(Transcription transcription);
    }
}
