package com.infantshervin.docshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginBtn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("DocShieldPrefs", MODE_PRIVATE);
        if (prefs.getString("token", null) != null) {
            startMainActivity();
        }

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.login_btn);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String pass = passwordInput.getText().toString();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, pass);
            }
        });

        findViewById(R.id.register_lbl).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void performLogin(String email, String pass) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.login(new ApiService.LoginRequest(email, pass)).enqueue(new Callback<ApiService.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiService.AuthResponse> call, Response<ApiService.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prefs.edit().putString("token", response.body().access_token).apply();
                    startMainActivity();
                } else {
                    // Try to register if login fails (simple demo flow)
                    api.register(new ApiService.RegisterRequest(email, pass)).enqueue(new Callback<ApiService.AuthResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.AuthResponse> call, Response<ApiService.AuthResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Registered! Please login.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiService.AuthResponse> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiService.AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
