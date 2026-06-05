package com.example.moviedates.view.AccessActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.AuthRequest;
import com.example.moviedates.network.model.AuthResponse;
import com.example.moviedates.view.PagesActivities.RoomActivity;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        String email = getIntent().getStringExtra("email");
        if (email != null) emailInput.setText(email);

        loginButton.setOnClickListener(v -> attemptLogin());

    }

    private void attemptLogin() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (password.isEmpty()) {
            passwordInput.setError("Please enter a password");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters");
            passwordInput.requestFocus();
            return;
        }

        setLoadingState(true);

        ApiService api = ApiClient.getInstance(this).create(ApiService.class);
        api.login(new AuthRequest(email, password, null))
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {

                        setLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            saveTokenAndProceed(response.body().getToken(), response.body().getUser().getId());
                            return;
                        }

                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed (" + response.code() + "). Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private void saveTokenAndProceed(String token, long userId) {

        SharedPreferences.Editor prefs = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).edit();
        prefs.putString("jwt_token", token);
        prefs.putLong("user_id", userId);
        prefs.apply();

        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, RoomActivity.class));
        finish();

    }

    private void setLoadingState(boolean loading) {

        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Logging in…" : "Log In");
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);

    }

}