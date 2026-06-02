package com.example.moviedates.view.AccessActivities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.google.android.material.button.MaterialButton;

public class GuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest);

        // Initialize Views
        EditText nicknameInput = findViewById(R.id.nicknameInput);

        EditText roomInput = findViewById(R.id.roomInput);

        MaterialButton joinButton = findViewById(R.id.joinButton);

        joinButton.setOnClickListener(v -> {

            String nickname = nicknameInput.getText().toString().trim();
            String room = roomInput.getText().toString().trim();

            // Nickname Empty
            if (nickname.isEmpty()) {
                nicknameInput.setError("Please enter a nickname");
                nicknameInput.requestFocus();
                return;
            }

            // Nickname length
            if (nickname.length() > 20) {
                nicknameInput.setError("Nickname too long");
                nicknameInput.requestFocus();
                return;
            }

            // Room Length
            if (room.length() != 6) {
                roomInput.setError("Room code must be 6 characters");
                roomInput.requestFocus();
                return;
            }

            Toast.makeText(GuestActivity.this, "Joining room!", Toast.LENGTH_SHORT).show();

        });

    }
}

