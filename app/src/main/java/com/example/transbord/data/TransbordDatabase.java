package com.example.transbord.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transcription.class}, version = 1, exportSchema = false)
public abstract class TransbordDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "transbord_db";
    private static TransbordDatabase instance;
    
    public abstract TranscriptionDao transcriptionDao();
    
    public static synchronized TransbordDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    TransbordDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
