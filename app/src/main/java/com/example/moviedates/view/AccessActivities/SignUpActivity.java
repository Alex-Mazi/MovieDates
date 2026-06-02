package com.example.moviedates.view.AccessActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.view.PagesActivities.GenresActivity;
import com.google.android.material.button.MaterialButton;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize Views
        EditText emailInput = findViewById(R.id.emailInput);

        String email = getIntent().getStringExtra("email");

        if (email != null) {
            emailInput.setText(email);
        }

        EditText passwordInput = findViewById(R.id.passwordInput);

        EditText confirmInput = findViewById(R.id.confirmInput);

        MaterialButton signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> {

            String password = passwordInput.getText().toString().trim();

            String confirmPassword = confirmInput.getText().toString().trim();

            // Password Empty
            if (password.isEmpty()) {
                passwordInput.setError("Please enter a password");
                passwordInput.requestFocus();
                return;
            }

            // Password Length
            if (password.length() < 8) {
                passwordInput.setError("Password must be at least 8 characters");
                passwordInput.requestFocus();
                return;
            }

            // Confirm Password Empty
            if (confirmPassword.isEmpty()) {
                confirmInput.setError("Please confirm your password");
                confirmInput.requestFocus();
                return;
            }

            // Password Match
            if (!password.equals(confirmPassword)) {
                confirmInput.setError("Passwords do not match");
                confirmInput.requestFocus();
                return;
            }

            Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

            // Move to GenresActivity
            Intent intent = new Intent(SignUpActivity.this, GenresActivity.class);

            startActivity(intent);

        });

    }

}
