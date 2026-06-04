package com.example.moviedates;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.view.AccessActivities.GuestActivity;
import com.example.moviedates.view.AccessActivities.LoginActivity;
import com.example.moviedates.view.AccessActivities.SignUpActivity;
import com.google.android.material.button.MaterialButton;

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

        googleButton.setOnClickListener(v -> Toast.makeText(this, "Google Sign-In — coming soon", Toast.LENGTH_SHORT).show());
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
}