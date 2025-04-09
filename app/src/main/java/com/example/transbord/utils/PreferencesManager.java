package com.example.transbord.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages app preferences and settings
 */
public class PreferencesManager {
    
    private static final String PREF_NAME = "transbord_preferences";
    private static final String KEY_FIRST_TIME = "is_first_time";
    
    private final SharedPreferences sharedPreferences;
    
    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Check if this is the first time the app is launched
     * @return true if first time, false otherwise
     */
    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }
    
    /**
     * Set the first time status
     * @param isFirstTime true if first time, false otherwise
     */
    public void setFirstTimeLaunch(boolean isFirstTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, isFirstTime);
        editor.apply();
    }
}
