package com.example.transbord.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TranscriptionDao {
    
    @Insert
    long insert(Transcription transcription);
    
    @Update
    void update(Transcription transcription);
    
    @Delete
    void delete(Transcription transcription);
    
    @Query("DELETE FROM transcriptions WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT * FROM transcriptions WHERE id = :id")
    Transcription getTranscriptionById(long id);
    
    @Query("SELECT * FROM transcriptions ORDER BY timestamp DESC")
    LiveData<List<Transcription>> getAllTranscriptions();
    
    @Query("SELECT * FROM transcriptions WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<Transcription>> searchTranscriptions(String query);
    
    @Query("DELETE FROM transcriptions")
    void deleteAllTranscriptions();
}
