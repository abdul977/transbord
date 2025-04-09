package com.example.transbord.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private boolean isRecording = false;
    private final Context context;
    
    public AudioRecorder(Context context) {
        this.context = context;
    }
    
    public boolean startRecording() {
        try {
            // Create output directory if it doesn't exist
            File outputDir = new File(context.getFilesDir(), "recordings");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Create output file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            outputFile = new File(outputDir, "recording_" + timestamp + ".m4a").getAbsolutePath();
            
            // Initialize MediaRecorder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(context);
            } else {
                mediaRecorder = new MediaRecorder();
            }
            
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(16000); // 16kHz as recommended for speech recognition
            mediaRecorder.setAudioEncodingBitRate(128000); // 128kbps for good quality
            mediaRecorder.setOutputFile(outputFile);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            
            Log.d(TAG, "Recording started: " + outputFile);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            releaseMediaRecorder();
            return false;
        }
    }
    
    public File stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Recording stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recording", e);
            }
            
            releaseMediaRecorder();
            isRecording = false;
            
            return new File(outputFile);
        }
        return null;
    }
    
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public String getOutputFile() {
        return outputFile;
    }
}
