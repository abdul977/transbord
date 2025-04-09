package com.example.transbord.api;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReasoningApiClient {
    private static final String TAG = "ReasoningApiClient";
    private static final String BASE_URL = "https://api.groq.com/";
    private static final String API_KEY = "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";
    
    private final ReasoningApiService apiService;
    
    public ReasoningApiClient() {
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
        apiService = retrofit.create(ReasoningApiService.class);
    }
    
    /**
     * Process text with reasoning model
     * @param text The text to process
     * @param instruction The instruction for processing (e.g., "correct this text", "summarize this text")
     * @param callback Callback for handling the response
     */
    public void processText(String text, String instruction, ReasoningCallback callback) {
        // Create request
        ReasoningRequest request = new ReasoningRequest.Builder()
                .model("deepseek-r1-distill-llama-70b") // Using DeepSeek R1 Distill Llama 70B model
                .addMessage("user", instruction + "\n\n" + text)
                .temperature(0.6)
                .maxCompletionTokens(1024)
                .topP(0.95)
                .reasoningFormat("raw") // Include reasoning in the response
                .build();
        
        // Create authorization header
        String authorization = "Bearer " + API_KEY;
        
        // Make API call
        Call<ReasoningResponse> call = apiService.processReasoning(authorization, request);
        
        call.enqueue(new Callback<ReasoningResponse>() {
            @Override
            public void onResponse(Call<ReasoningResponse> call, Response<ReasoningResponse> response) {
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
            public void onFailure(Call<ReasoningResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onFailure(new Exception("Network Error: " + t.getMessage()));
            }
        });
    }
    
    public interface ReasoningCallback {
        void onSuccess(ReasoningResponse response);
        void onFailure(Exception e);
    }
}
