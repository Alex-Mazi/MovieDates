package com.example.moviedates.view.PagesActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.SessionResponse;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomActivity extends AppCompatActivity {

    private EditText roomInput;
    private MaterialButton createButton;
    private MaterialButton joinButton;
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.room);

        createButton = findViewById(R.id.createButton);
        roomInput = findViewById(R.id.roomInput);
        joinButton = findViewById(R.id.joinButton);

        userId = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getLong("user_id", -1);

        createButton.setOnClickListener(v -> attemptCreate());
        joinButton.setOnClickListener(v -> attemptJoin());

    }

    // ── Create ────────────────────────────────────────────────────────────────

    private void attemptCreate() {

        setLoadingState(true);

        ApiClient.getInstance(this).create(ApiService.class).createRoom()
                .enqueue(new Callback<SessionResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {

                        setLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(RoomActivity.this, "Room created!", Toast.LENGTH_LONG).show();
                            navigateToSwipe(response.body().getCode());
                        } else {
                            Toast.makeText(RoomActivity.this, "Failed to create room (" + response.code() + ")", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        Toast.makeText(RoomActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private void attemptJoin() {

        String code = roomInput.getText().toString().trim();

        if (code.length() != 6) {
            roomInput.setError("Room code must be 6 characters");
            roomInput.requestFocus();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);

        Map<String, Long> body = new HashMap<>();
        body.put("userId", userId);

        ApiClient.getInstance(this).create(ApiService.class).joinRoom(code, body)
                .enqueue(new Callback<SessionResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {

                        setLoadingState(false);

                        if (response.isSuccessful() && response.body() != null) {
                            navigateToSwipe(response.body().getCode());
                        } else if (response.code() == 404) {
                            roomInput.setError("Room not found");
                            roomInput.requestFocus();
                        } else {
                            Toast.makeText(RoomActivity.this, "Failed to join room (" + response.code() + ")", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        Toast.makeText(RoomActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private void navigateToSwipe(String roomCode) {

        getSharedPreferences("moviedates_prefs", MODE_PRIVATE).edit().putString("room_code", roomCode).apply();

        // Goto CodeActivity
        Intent intent = new Intent(this, CodeActivity.class);
        intent.putExtra("room_code", roomCode);
        startActivity(intent);
        finish();

    }

    private void setLoadingState(boolean loading) {

        createButton.setEnabled(!loading);
        joinButton.setEnabled(!loading);
        roomInput.setEnabled(!loading);
        createButton.setText(loading ? "Creating…" : "Create Room");
        joinButton.setText(loading ? "Joining…"  : "Join Room");

    }

}