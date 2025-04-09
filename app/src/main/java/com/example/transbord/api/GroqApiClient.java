package com.example.transbord.api;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GroqApiClient {
    private static final String TAG = "GroqApiClient";
    private static final String BASE_URL = "https://api.groq.com/";
    private static final String API_KEY = "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";
    private static final String MODEL = "whisper-large-v3-turbo";
    
    private final GroqApiService apiService;
    
    public GroqApiClient() {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create OkHttpClient with the interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // Create API service
        apiService = retrofit.create(GroqApiService.class);
    }
    
    public void transcribeAudio(File audioFile, String language, TranscriptionCallback callback) {
        // Create request parts
        RequestBody modelBody = RequestBody.create(MediaType.parse("text/plain"), MODEL);
        RequestBody responseFormatBody = RequestBody.create(MediaType.parse("text/plain"), "verbose_json");
        RequestBody languageBody = RequestBody.create(MediaType.parse("text/plain"), language);
        
        // Create file part
        RequestBody fileBody = RequestBody.create(MediaType.parse("audio/*"), audioFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", audioFile.getName(), fileBody);
        
        // Create authorization header
        String authorization = "Bearer " + API_KEY;
        
        // Make API call
        Call<TranscriptionResponse> call = apiService.transcribeAudio(
                authorization,
                filePart,
                modelBody,
                responseFormatBody,
                languageBody
        );
        
        call.enqueue(new Callback<TranscriptionResponse>() {
            @Override
            public void onResponse(Call<TranscriptionResponse> call, Response<TranscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onFailure(new Exception("API Error: " + response.code() + " - " + errorBody));
                    } catch (IOException e) {
                        callback.onFailure(new Exception("API Error: " + response.code()));
                    }
                }
            }
            
            @Override
            public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onFailure(new Exception("Network Error: " + t.getMessage()));
            }
        });
    }
    
    public interface TranscriptionCallback {
        void onSuccess(TranscriptionResponse response);
        void onFailure(Exception e);
    }
}
