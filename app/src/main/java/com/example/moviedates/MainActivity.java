package com.example.moviedates;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.AuthResponse;
import com.example.moviedates.view.AccessActivities.GuestActivity;
import com.example.moviedates.view.AccessActivities.LoginActivity;
import com.example.moviedates.view.AccessActivities.SignUpActivity;
import com.example.moviedates.view.PagesActivities.GenresActivity;
import com.example.moviedates.view.PagesActivities.RoomActivity;
import com.google.android.material.button.MaterialButton;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.Credential;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private MaterialButton continueAsGuestButton;
    private LinearLayout googleButton;

    private CredentialManager credentialManager;
    private static final String WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        credentialManager = CredentialManager.create(this);
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
            //Goto SignUpActivity
            navigateTo(SignUpActivity.class, email);
        });

        loginButton.setOnClickListener(v -> {
            String email = getValidatedEmail();
            if (email == null) return;
            //Goto LoginActivity
            navigateTo(LoginActivity.class, email);
        });

        //Goto GuestActivity
        continueAsGuestButton.setOnClickListener(v -> navigateTo(GuestActivity.class, null));

        googleButton.setOnClickListener(v -> startGoogleSignIn());

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
        if (email != null) intent.putExtra("email", email);
        startActivity(intent);
    }

    private void startGoogleSignIn() {

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(WEB_CLIENT_ID).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();

        credentialManager.getCredentialAsync(this, request, null, Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof GoogleIdTokenCredential) {
                            String idToken = ((GoogleIdTokenCredential) credential).getIdToken();
                            sendTokenToBackend(idToken);
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                }
        );
    }

    private void sendTokenToBackend(String idToken) {
        ApiService api = ApiClient.getInstance(this).create(ApiService.class);
        api.googleLogin(Map.of("idToken", idToken))
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            saveTokenAndProceed(response.body().getToken(), response.body().getUser().getId(), response.body().isNewUser());
                            return;
                        }
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Google Sign-In failed (" + response.code() + ").", Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
    }

    private void saveTokenAndProceed(String token, long userId, boolean isNewUser) {
        SharedPreferences.Editor prefs = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).edit();
        prefs.putString("jwt_token", token);
        prefs.putLong("user_id", userId);
        prefs.apply();

        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
        // Goto GenresActivity or RoomActivity
        Class<?> destination = isNewUser ? GenresActivity.class : RoomActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }

}