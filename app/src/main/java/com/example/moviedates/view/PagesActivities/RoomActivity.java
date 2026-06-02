package com.example.moviedates.view.PagesActivities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.google.android.material.button.MaterialButton;

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room);

        MaterialButton createButton = findViewById(R.id.createButton);

        EditText roomInput = findViewById(R.id.roomInput);

        MaterialButton joinButton = findViewById(R.id.joinButton);

        createButton.setOnClickListener(v -> {

            String room = roomInput.getText().toString().trim();

            // Room Length
            if (room.length() != 6) {
                roomInput.setError("Room code must be 6 characters");
                roomInput.requestFocus();
                return;
            }

            Toast.makeText(RoomActivity.this, "Creating a room!", Toast.LENGTH_SHORT).show();

        });

        joinButton.setOnClickListener(v -> {

            String room = roomInput.getText().toString().trim();

            // Room Length
            if (room.length() != 6) {
                roomInput.setError("Room code must be 6 characters");
                roomInput.requestFocus();
                return;
            }

            Toast.makeText(RoomActivity.this, "Joining room!", Toast.LENGTH_SHORT).show();

        });

    }
}


