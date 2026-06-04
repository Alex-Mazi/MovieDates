package com.example.moviedates.view.AccessActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.AuthResponse;
import com.example.moviedates.view.PagesActivities.RoomActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestActivity extends AppCompatActivity {

    private EditText nicknameInput;
    private EditText roomInput;
    private MaterialButton joinButton;

    private String guestToken = null;
    private long guestUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest);

        nicknameInput = findViewById(R.id.nicknameInput);
        roomInput = findViewById(R.id.roomInput);
        joinButton = findViewById(R.id.joinButton);

        // Register guest as soon as nickname loses focus
        nicknameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String nickname = nicknameInput.getText().toString().trim();
                if (!nickname.isEmpty() && nickname.length() <= 20) {
                    registerGuest(nickname);
                }
            }
        });

        joinButton.setOnClickListener(v -> attemptJoin());
    }

    private void registerGuest(String nickname) {
        // Reset in case nickname changed
        guestToken = null;
        guestUserId = -1;

        ApiService api = ApiClient.getInstance(this).create(ApiService.class);
        api.registerGuest(Map.of("nickname", nickname))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            guestToken = response.body().getToken();
                            guestUserId = response.body().getUser().getId();

                            getSharedPreferences("moviedates_prefs", MODE_PRIVATE).edit().putString("jwt_token", guestToken).apply();

                            // Lock the nickname — it's been registered, can't change it now
                            nicknameInput.setEnabled(false);
                            nicknameInput.setAlpha(0.6f);
                        } else {
                            Toast.makeText(GuestActivity.this, "Could not register guest. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        Toast.makeText(GuestActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptJoin() {
        String nickname = nicknameInput.getText().toString().trim();
        String room = roomInput.getText().toString().trim();

        if (nickname.isEmpty()) {
            nicknameInput.setError("Please enter a nickname");
            nicknameInput.requestFocus();
            return;
        }
        if (nickname.length() > 20) {
            nicknameInput.setError("Nickname too long");
            nicknameInput.requestFocus();
            return;
        }
        if (room.length() != 6) {
            roomInput.setError("Room code must be 6 characters");
            roomInput.requestFocus();
            return;
        }

        // If guest wasn't registered yet (user jumped straight to join), register first
        if (guestToken == null || guestUserId == -1) {
            Toast.makeText(this, "Please wait, setting up your guest account…", Toast.LENGTH_SHORT).show();
            registerGuest(nickname);
            return;
        }

        // All good — go to RoomActivity with userId + room code
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("userId", guestUserId);
        intent.putExtra("roomCode", room);
        startActivity(intent);
    }
}