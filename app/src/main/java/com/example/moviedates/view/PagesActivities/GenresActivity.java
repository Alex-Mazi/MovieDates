package com.example.moviedates.view.PagesActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.AuthResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenresActivity extends AppCompatActivity {

    private int selectedGenresCount = 0;
    private final List<Integer> selectedGenreIds = new ArrayList<>();
    private final List<MaterialCardView> genreCards = new ArrayList<>();
    private GridLayout genreGrid;
    private MaterialButton nextButton;

    private static final Map<String, Integer> GENRE_NAME_TO_DRAWABLE = new HashMap<String, Integer>() {{
        put("Action", R.drawable.genre_action);
        put("Adventure", R.drawable.genre_adventure);
        put("Animation", R.drawable.genre_animation);
        put("Comedy", R.drawable.genre_comedy);
        put("Crime", R.drawable.genre_crime);
        put("Documentary", R.drawable.genre_documentary);
        put("Drama", R.drawable.genre_drama);
        put("Family", R.drawable.genre_family);
        put("Fantasy", R.drawable.genre_fantasy);
        put("History", R.drawable.genre_history);
        put("Horror", R.drawable.genre_horror);
        put("Music", R.drawable.genre_music);
        put("Mystery", R.drawable.genre_mystery);
        put("Romance", R.drawable.genre_romance);
        put("Science Fiction", R.drawable.genre_sci);
        put("Thriller", R.drawable.genre_thriller);
        put("War", R.drawable.genre_war);
        put("Western", R.drawable.genre_western);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.genres);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setAlpha(0.5f);
        genreGrid = findViewById(R.id.genreGrid);

        nextButton.setOnClickListener(v -> {
            if (selectedGenresCount < 3) {
                Toast.makeText(this, "Please select at least 3 genres", Toast.LENGTH_SHORT).show();
            } else {
                saveGenres(nextButton);
            }
        });

        fetchGenres();

    }

    private void fetchGenres() {

        ApiClient.getInstance(this).create(ApiService.class)
                .getGenres()
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            List<?> genres = (List<?>) response.body().get("genres");

                            if (genres != null) {

                                genreGrid.removeAllViews();

                                for (Object item : genres) {
                                    if (item instanceof Map) {
                                        Map<?, ?> genre = (Map<?, ?>) item;
                                        int id = ((Double) Objects.requireNonNull(genre.get("id"))).intValue();
                                        String name = (String) genre.get("name");
                                        Integer drawable = GENRE_NAME_TO_DRAWABLE.get(name);
                                        if (drawable != null) {addGenreCard(id, name, drawable);}
                                    }
                                }

                            }

                        } else {
                            Toast.makeText(GenresActivity.this, "Failed to load genres", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        Toast.makeText(GenresActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private void addGenreCard(int genreId, String genreName, int drawableRes) {

        float dp = getResources().getDisplayMetrics().density;

        MaterialCardView card = new MaterialCardView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = (int) (150 * dp);
        params.height = (int) (100 * dp);
        params.setMargins((int)(8*dp), (int)(8*dp), (int)(8*dp), (int)(8*dp));
        card.setLayoutParams(params);
        card.setRadius(12 * dp);
        card.setCardElevation(6 * dp);

        FrameLayout frame = new FrameLayout(this);
        frame.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        frame.setBackgroundResource(drawableRes);

        TextView label = new TextView(this);
        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.BOTTOM | android.view.Gravity.START);
        label.setLayoutParams(labelParams);
        label.setPadding((int)(12*dp), 0, (int)(12*dp), (int)(12*dp));
        label.setText(genreName);
        label.setTextColor(android.graphics.Color.WHITE);
        label.setTextSize(18);
        label.setTypeface(null, android.graphics.Typeface.BOLD);

        frame.addView(label);
        card.addView(frame);

        card.setTag(false);
        card.setStrokeWidth(0);

        card.setOnClickListener(v -> {

            boolean selected = (boolean) card.getTag();
            selected = !selected;
            card.setTag(selected);

            if (selected) {
                selectedGenresCount++;
                selectedGenreIds.add(genreId);
                card.setStrokeWidth(6);
                card.setStrokeColor(android.graphics.Color.WHITE);
                card.setCardElevation(14f);
                card.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();
            } else {
                selectedGenresCount--;
                selectedGenreIds.remove(Integer.valueOf(genreId));
                card.setStrokeWidth(0);
                card.setCardElevation(6f);
                card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }

            updateNextButton();

        });

        genreCards.add(card);
        genreGrid.addView(card);

    }

    @SuppressLint("SetTextI18n")
    private void saveGenres(MaterialButton nextButton) {

        SharedPreferences prefs = getSharedPreferences("moviedates_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        nextButton.setEnabled(false);
        nextButton.setText("Saving…");

        ApiClient.getInstance(this).create(ApiService.class).updateGenres(userId, selectedGenreIds)
                .enqueue(new Callback<AuthResponse.UserPayload>() {

                    @Override
                    public void onResponse(@NonNull Call<AuthResponse.UserPayload> call, @NonNull Response<AuthResponse.UserPayload> response) {

                        nextButton.setEnabled(true);
                        nextButton.setText("Next");

                        if (response.isSuccessful()) {
                            Intent intent = new Intent(GenresActivity.this, PersonalSwipeActivity.class);
                            intent.putIntegerArrayListExtra("genreIds", new ArrayList<>(selectedGenreIds));
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(GenresActivity.this, "Failed to save genres (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse.UserPayload> call, @NonNull Throwable t) {
                        nextButton.setEnabled(true);
                        nextButton.setText("Next");
                        Toast.makeText(GenresActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });

    }

    private void updateNextButton() {

        float alpha;

        switch (selectedGenresCount) {
            case 0: alpha = 0.35f; break;
            case 1: alpha = 0.55f; break;
            case 2: alpha = 0.75f; break;
            default: alpha = 1.0f; break;
        }

        nextButton.animate().alpha(alpha).setDuration(150).start();

    }

}