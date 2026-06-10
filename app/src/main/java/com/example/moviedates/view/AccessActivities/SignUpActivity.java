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
import com.example.moviedates.view.PagesActivities.GenresActivity;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private MaterialButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        signUpButton = findViewById(R.id.signUpButton);

        String email = getIntent().getStringExtra("email");
        if (email != null) emailInput.setText(email);

        signUpButton.setOnClickListener(v -> attemptSignUp());

    }

    private void attemptSignUp() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm  = confirmInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Please enter an email");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

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

        if (confirm.isEmpty()) {
            confirmInput.setError("Please confirm your password");
            confirmInput.requestFocus();
            return;
        }

        if (!password.equals(confirm)) {
            confirmInput.setError("Passwords do not match");
            confirmInput.requestFocus();
            return;
        }

        // Uses the email prefix as display name
        String displayName = email.split("@")[0];

        setLoadingState(true);

        ApiService api = ApiClient.getInstance(this).create(ApiService.class);

        api.register(new AuthRequest(email, password, displayName))
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {

                        setLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            saveTokenAndProceed(response.body().getToken(), response.body().getUser().getId());
                            return;
                        }

                        // "Email already exists" : send them to login
                        if (response.code() == 500) {
                            Toast.makeText(SignUpActivity.this, "This email is already registered. Please log in.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            intent.putExtra("email", emailInput.getText().toString().trim());
                            startActivity(intent);
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign-up failed (" + response.code() + "). Please try again.", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private void saveTokenAndProceed(String token, long userId) {

        SharedPreferences.Editor prefs = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).edit();
        prefs.putString("jwt_token", token);
        prefs.putLong("user_id", userId);
        prefs.apply();

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
        // Goto GenresActivity
        startActivity(new Intent(this, GenresActivity.class));
        finish();

    }

    private void setLoadingState(boolean loading) {

        signUpButton.setEnabled(!loading);
        signUpButton.setText(loading ? "Creating account…" : "Sign Up");
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        confirmInput.setEnabled(!loading);

    }

}