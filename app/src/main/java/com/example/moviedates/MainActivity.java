package com.example.moviedates;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.CheckEmailRequest;
import com.example.moviedates.network.model.CheckEmailResponse;
import com.example.moviedates.view.AccessActivities.GuestActivity;
import com.example.moviedates.view.AccessActivities.LoginActivity;
import com.example.moviedates.view.AccessActivities.SignUpActivity;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private MaterialButton continueAsGuestButton;
    private LinearLayout googleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setClickListeners();
    }

    private void bindViews() {
        emailInput = findViewById(R.id.emailInput);
        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);
        continueAsGuestButton = findViewById(R.id.continueAsGuestButton);
        googleButton = findViewById(R.id.googleButton);
    }

    private void setClickListeners() {

        signUpButton.setOnClickListener(v -> {
            String email = getValidatedEmail();
            if (email == null) return;

            checkEmailThenNavigate(email, true);
        });

        loginButton.setOnClickListener(v -> {
            String email = getValidatedEmail();
            if (email == null) return;

            checkEmailThenNavigate(email, false);
        });

        continueAsGuestButton.setOnClickListener(v -> navigateTo(GuestActivity.class, null));

        googleButton.setOnClickListener(v -> {Toast.makeText(this, "Google Sign-In — coming soon", Toast.LENGTH_SHORT).show();});
    }

    private void checkEmailThenNavigate(String email, boolean calledFromSignUp) {
        setLoadingState(true);

        ApiService api = ApiClient.getInstance(this).create(ApiService.class);
        Call<CheckEmailResponse> call = api.checkEmail(new CheckEmailRequest(email));

        call.enqueue(new Callback<CheckEmailResponse>() {

            @Override
            public void onResponse(@NonNull Call<CheckEmailResponse> call, @NonNull Response<CheckEmailResponse> response) {
                setLoadingState(false);

                if (!response.isSuccessful() || response.body() == null) {
                    showError("Server error (" + response.code() + "). Please try again.");
                    return;
                }

                boolean emailExists = response.body().isExists();

                if (calledFromSignUp) {
                    if (emailExists) {
                        showError("This email is already registered. Please log in.");
                        navigateTo(LoginActivity.class, email);
                    } else {
                        navigateTo(SignUpActivity.class, email);
                    }
                } else {
                    if (emailExists) {
                        navigateTo(LoginActivity.class, email);
                    } else {
                        showError("No account found. Please sign up first.");
                        navigateTo(SignUpActivity.class, email);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckEmailResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private String getValidatedEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Please enter an email");
            emailInput.requestFocus();
            return null;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return null;
        }

        return email;
    }

    private void navigateTo(Class<?> target, String email) {
        Intent intent = new Intent(this, target);
        if (email != null) {
            intent.putExtra("email", email);
        }
        startActivity(intent);
    }

    private void setLoadingState(boolean loading) {
        signUpButton.setEnabled(!loading);
        loginButton.setEnabled(!loading);
        continueAsGuestButton.setEnabled(!loading);
        googleButton.setEnabled(!loading);
    }

    private void showError(String message) {Toast.makeText(this, message, Toast.LENGTH_LONG).show();}
}