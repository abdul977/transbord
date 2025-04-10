package com.example.transbord.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface GroqApiService {
    
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    Call<TranscriptionResponse> transcribeAudio(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part file,
            @Part("model") RequestBody model,
            @Part("response_format") RequestBody responseFormat,
            @Part("language") RequestBody language
    );
}
