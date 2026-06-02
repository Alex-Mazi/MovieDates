package com.example.moviedates.view.AccessActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;

import com.example.moviedates.view.PagesActivities.RoomActivity;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Views
        EditText emailInput = findViewById(R.id.emailInput);

        String email = getIntent().getStringExtra("email");

        if (email != null) {
            emailInput.setText(email);
        }

        EditText passwordInput = findViewById(R.id.passwordInput);

        MaterialButton loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {

            String password = passwordInput.getText().toString().trim();

            // Password Empty
            if (password.isEmpty()) {
                passwordInput.setError("Please enter a password");
                passwordInput.requestFocus();
                return;
            }

            // Password Length
            if (password.length() < 8) {
                passwordInput.setError("Password must be at least 6 characters");
                passwordInput.requestFocus();
                return;
            }

            Toast.makeText(LoginActivity.this, "Logging in to account!", Toast.LENGTH_SHORT).show();

            // Move to RoomActivity
            Intent intent = new Intent(LoginActivity.this, RoomActivity.class);

            startActivity(intent);

        });

    }
}

