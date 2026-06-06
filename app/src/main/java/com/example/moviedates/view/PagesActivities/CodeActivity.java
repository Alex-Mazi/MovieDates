package com.example.moviedates.view.PagesActivities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.AuthResponse;
import com.example.moviedates.network.model.SessionResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodeActivity extends AppCompatActivity {

    private ImageView copyIcon;
    private LinearLayout avatarContainer;
    private MaterialButton startButton;

    private String roomCode;
    private boolean copied = false;
    private boolean polling = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int POLL_INTERVAL = 4_000;

    private static final int[] AVATAR_COLORS = { 0xFFE91E63, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFF9C27B0 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.code);

        TextView roomCodeText = findViewById(R.id.roomCodeText);
        copyIcon = findViewById(R.id.copyIcon);
        avatarContainer = findViewById(R.id.avatarContainer);
        startButton = findViewById(R.id.loginButton);

        roomCode = getIntent().getStringExtra("room_code");
        if (roomCode == null) {
            roomCode = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getString("room_code", "------");
        }

        roomCodeText.setText(roomCode);

        copyIcon.setOnClickListener(v -> copyCode());
        roomCodeText.setOnClickListener(v -> copyCode());

        startButton.setOnClickListener(v -> startSession());

        startPolling();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }

    private void copyCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Room Code", roomCode);
        clipboard.setPrimaryClip(clip);

        if (!copied) {
            copied = true;
            copyIcon.setImageResource(R.drawable.ic_check);
        }

        Toast.makeText(this, "Code copied!", Toast.LENGTH_SHORT).show();
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!polling) return;
            fetchSession();
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    private void startPolling() {
        polling = true;
        handler.post(pollRunnable);
    }

    private void stopPolling() {
        polling = false;
        handler.removeCallbacks(pollRunnable);
    }

    private void fetchSession() {
        long userId = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getLong("user_id", -1);

        java.util.Map<String, Long> body = new java.util.HashMap<>();
        body.put("userId", userId);

        ApiClient.getInstance(this).create(ApiService.class).joinRoom(roomCode, body)
                .enqueue(new Callback<SessionResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            renderAvatars(response.body().getParticipants());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) { }
                });
    }

    @SuppressLint("SetTextI18n")
    private void startSession() {

        stopPolling();

        startButton.setEnabled(false);
        startButton.setText("Starting…");

        ApiClient.getInstance(this).create(ApiService.class).startSession(roomCode)
                .enqueue(new Callback<SessionResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {

                        if (response.isSuccessful()) {
                            navigateToSwipe();
                        } else {
                            startButton.setEnabled(true);
                            startButton.setText(getString(R.string.start_swiping));
                            Toast.makeText(CodeActivity.this, "Could not start session (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            startPolling();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {

                        startButton.setEnabled(true);
                        startButton.setText(getString(R.string.start_swiping));
                        Toast.makeText(CodeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        startPolling();

                    }

                });

    }

    private void navigateToSwipe() {
        Intent intent = new Intent(this, GroupSwipeActivity.class);
        intent.putExtra("room_code", roomCode);
        startActivity(intent);
        finish();
    }

    private void renderAvatars(java.util.List<AuthResponse.UserPayload> participants) {

        if (participants == null) return;

        avatarContainer.removeAllViews();

        int size   = dpToPx(48);
        int margin = dpToPx(6);

        for (int i = 0; i < participants.size(); i++) {
            AuthResponse.UserPayload p = participants.get(i);
            int color = AVATAR_COLORS[i % AVATAR_COLORS.length];

            View ring = new View(this);
            LinearLayout.LayoutParams ringParams = new LinearLayout.LayoutParams(size + dpToPx(4), size + dpToPx(4));
            ringParams.setMargins(margin, 0, margin, 0);
            ring.setLayoutParams(ringParams);
            GradientDrawable ringBg = new GradientDrawable();
            ringBg.setShape(GradientDrawable.OVAL);
            ringBg.setColor(color);
            ring.setBackground(ringBg);

            TextView initial = new TextView(this);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(size, size);
            tvParams.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            initial.setLayoutParams(tvParams);
            initial.setGravity(android.view.Gravity.CENTER);
            initial.setTextColor(Color.WHITE);
            initial.setTextSize(18);
            initial.setTypeface(null, android.graphics.Typeface.BOLD);

            String name = p.getDisplayName();

            if (name == null || name.isEmpty()) {
                String email = p.getEmail();
                name = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : email;
            }

            initial.setText(name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

            GradientDrawable innerBg = new GradientDrawable();
            innerBg.setShape(GradientDrawable.OVAL);
            innerBg.setColor(darken(color));
            initial.setBackground(innerBg);

            android.widget.FrameLayout frame = new android.widget.FrameLayout(this);
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(size + dpToPx(4), size + dpToPx(4));
            frameParams.setMargins(margin, 0, margin, 0);
            frame.setLayoutParams(frameParams);
            frame.addView(ring, new android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
            frame.addView(initial, new android.widget.FrameLayout.LayoutParams(size, size, android.view.Gravity.CENTER));

            avatarContainer.addView(frame);

        }

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= (1f - (float) 0.15);
        return Color.HSVToColor(hsv);
    }

}