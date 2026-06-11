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
import com.example.moviedates.network.model.MovieDTO;
import com.example.moviedates.network.model.SessionResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CodeActivity extends AppCompatActivity {

    private ImageView copyIcon;
    private LinearLayout avatarContainer;
    private MaterialButton startButton;
    private Socket socket;
    private String roomCode;
    private boolean copied = false;
    private boolean polling = false;

    private Call<SessionResponse> pollCall;
    private Call<SessionResponse> startCall;

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
        connectSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        if (pollCall != null) pollCall.cancel();
        if (startCall != null) startCall.cancel();
        if (socket != null) {
            socket.disconnect();
            socket.off();
        }
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

        if (isDestroyed() || isFinishing()) return;

        pollCall = ApiClient.getInstance(this).create(ApiService.class).getSession(roomCode);
        pollCall.enqueue(new Callback<SessionResponse>() {

            @Override
            public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderAvatars(response.body().getParticipants());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void startSession() {
        stopPolling();
        startButton.setEnabled(false);
        startButton.setText("Starting…");

        startCall = ApiClient.getInstance(this).create(ApiService.class).startSession(roomCode);
        startCall.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(@NonNull Call<SessionResponse> call, @NonNull Response<SessionResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    fetchDeckAndBroadcast();
                } else {
                    startButton.setEnabled(true);
                    startButton.setText(getString(R.string.start_swiping));
                    Toast.makeText(CodeActivity.this, "Could not start session (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    startPolling();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessionResponse> call, @NonNull Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                startButton.setEnabled(true);
                startButton.setText(getString(R.string.start_swiping));
                Toast.makeText(CodeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                startPolling();
            }
        });
    }

    private void fetchDeckAndBroadcast() {
        ApiClient.getInstance(this).create(ApiService.class).getDeck(roomCode)
                .enqueue(new Callback<List<MovieDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<MovieDTO>> call, @NonNull Response<List<MovieDTO>> response) {
                        if (isDestroyed() || isFinishing()) return;

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            if (socket != null && socket.connected()) {
                                try {
                                    org.json.JSONObject payload = new org.json.JSONObject();
                                    payload.put("roomCode", roomCode);
                                    org.json.JSONArray moviesArray = new org.json.JSONArray();
                                    for (MovieDTO movie : response.body()) {
                                        org.json.JSONObject m = new org.json.JSONObject();
                                        m.put("id", movie.getId());
                                        m.put("title", movie.getTitle());
                                        m.put("posterPath", movie.getPosterPath());
                                        m.put("overview", movie.getOverview());
                                        m.put("voteAverage", movie.getVoteAverage());
                                        m.put("releaseDate", movie.getReleaseDate());
                                        moviesArray.put(m);
                                    }
                                    payload.put("movies", moviesArray);
                                    socket.emit("start_session", payload);
                                } catch (Exception e) {
                                    android.util.Log.e("CodeActivity", "Socket emit failed: " + e.getMessage());
                                }
                            }
                            navigateToSwipe(response.body()); // always navigate, socket is optional
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(CodeActivity.this,
                                        "No movies available. Try again.", Toast.LENGTH_LONG).show();
                                startButton.setEnabled(true);
                                startButton.setText(getString(R.string.start_swiping));
                                startPolling();
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<MovieDTO>> call, @NonNull Throwable t) {
                        if (isDestroyed() || isFinishing()) return;
                        runOnUiThread(() -> {
                            Toast.makeText(CodeActivity.this,
                                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            startButton.setEnabled(true);
                            startButton.setText(getString(R.string.start_swiping));
                            startPolling();
                        });
                    }
                });
    }

    private void connectSocket() {
        try {
            socket = IO.socket("http://129.159.31.140:9092");

            socket.on(Socket.EVENT_CONNECT, args -> {
                socket.emit("join_room", roomCode);
                android.util.Log.d("CodeActivity", "Socket connected, joined room: " + roomCode);
            });

            socket.on("session_started", args -> {
                if (isDestroyed() || isFinishing()) return;
                runOnUiThread(() -> {
                    stopPolling();
                    navigateToSwipe(null);
                });
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                    android.util.Log.e("CodeActivity", "Socket error: " + args[0]));

            socket.connect();

        } catch (Exception e) {
            android.util.Log.e("CodeActivity", "Socket init failed: " + e.getMessage());
        }
    }

    private void navigateToSwipe(List<MovieDTO> deck) {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
        }
        Intent intent = new Intent(this, GroupSwipeActivity.class);
        intent.putExtra("room_code", roomCode);
        if (deck != null) {
            intent.putParcelableArrayListExtra("deck", new ArrayList<>(deck));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void renderAvatars(java.util.List<AuthResponse.UserPayload> participants) {

        if (participants == null) return;

        avatarContainer.removeAllViews();

        int size = dpToPx(52);
        int overlap = dpToPx(18);

        int[][] gradientPairs = {
                { 0xFFE91E63, 0xFFFF6090 },
                { 0xFF2196F3, 0xFF64B5F6 },
                { 0xFF4CAF50, 0xFF81C784 },
                { 0xFFFF9800, 0xFFFFCC02 },
                { 0xFF9C27B0, 0xFFCE93D8 },
        };

        for (int i = 0; i < participants.size(); i++) {

            AuthResponse.UserPayload p = participants.get(i);
            int[] pair = gradientPairs[i % gradientPairs.length];

            View ring = new View(this);
            android.widget.FrameLayout.LayoutParams ringParams = new android.widget.FrameLayout.LayoutParams(size + dpToPx(6), size + dpToPx(6));
            GradientDrawable ringBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ pair[0], pair[1] });
            ringBg.setShape(GradientDrawable.OVAL);
            ringBg.setStroke(dpToPx(2), 0x33FFFFFF);
            ring.setBackground(ringBg);
            ring.setLayoutParams(ringParams);

            android.widget.FrameLayout innerCircle = new android.widget.FrameLayout(this);
            android.widget.FrameLayout.LayoutParams innerParams = new android.widget.FrameLayout.LayoutParams(size, size, android.view.Gravity.CENTER);
            innerCircle.setLayoutParams(innerParams);

            GradientDrawable innerBg = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[]{ darken(pair[0]), darken(pair[1]) });
            innerBg.setShape(GradientDrawable.OVAL);
            innerCircle.setBackground(innerBg);

            String name = p.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = p.getEmail();
                name = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : email;
            }

            TextView initial = new TextView(this);
            android.widget.FrameLayout.LayoutParams tvParams = new android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.view.Gravity.CENTER);
            initial.setLayoutParams(tvParams);
            initial.setGravity(android.view.Gravity.CENTER);
            initial.setTextColor(Color.WHITE);
            initial.setTextSize(20);
            initial.setTypeface(null, android.graphics.Typeface.BOLD);
            initial.setShadowLayer(dpToPx(2), 0, dpToPx(1), 0x55000000);
            initial.setText(name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

            innerCircle.addView(initial);

            android.widget.FrameLayout frame = new android.widget.FrameLayout(this);
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(size + dpToPx(6), size + dpToPx(6));

            if (i > 0) frameParams.setMarginStart(-overlap);

            frame.setElevation(dpToPx(i + 1));
            frame.setLayoutParams(frameParams);
            frame.addView(ring);
            frame.addView(innerCircle);

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
        hsv[2] *= (1f - 0.15f);
        return Color.HSVToColor(hsv);
    }
}