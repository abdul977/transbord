package com.example.transbord.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ReasoningApiService {
    
    @POST("openai/v1/chat/completions")
    Call<ReasoningResponse> processReasoning(
            @Header("Authorization") String authorization,
            @Body ReasoningRequest request
    );
}
