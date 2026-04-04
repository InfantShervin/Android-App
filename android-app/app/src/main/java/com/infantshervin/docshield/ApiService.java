package com.infantshervin.docshield;

import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @Multipart
    @POST("api/analyze/")
    Call<AnalysisResponse> analyze(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file
    );

    @GET("api/history/")
    Call<List<AnalysisResponse>> getHistory(
            @Header("Authorization") String token
    );

    // Request Models
    class RegisterRequest {
        public String email;
        public String name;
        public String password;
        public RegisterRequest(String email, String password) {
            this.email = email;
            this.name = email.split("@")[0]; // Use part of email as name
            this.password = password;
        }
    }

    class LoginRequest {
        public String email;
        public String password;
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    // Response Models
    class AuthResponse {
        public String access_token;
        public String token_type;
    }

    class Entity {
        public String text;
        public String label;
        public List<Integer> bbox;
        public String sensitivity;
        public double risk_score;
        public List<String> matched_types;
    }

    class AnalysisResponse {
        public int scan_id;
        public String filename;
        public String file_type;
        public double exposure_score;
        public String risk_level;
        public List<Entity> entities;
        public String raw_text;
        public String summary;
        public List<String> warnings;
        public List<String> safe_fields;
        public int sensitive_count;
        public int total_count;
    }
}
