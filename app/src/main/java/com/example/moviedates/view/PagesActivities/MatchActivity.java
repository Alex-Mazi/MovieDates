package com.example.moviedates.view.PagesActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moviedates.R;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.ApiClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MatchActivity extends AppCompatActivity {

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);

        ImageView moviePoster = findViewById(R.id.moviePoster);
        TextView matchText = findViewById(R.id.matchText);

        String title  = getIntent().getStringExtra("movie_title");
        String posterPath = getIntent().getStringExtra("movie_poster");
        String movieId = getIntent().getStringExtra("movie_id");

        if (posterPath != null && !posterPath.isEmpty()) {
            String url = posterPath.startsWith("http") ? posterPath : TMDB_IMAGE_BASE + posterPath;
            Glide.with(this).load(url).centerCrop().into(moviePoster);
        }

        KonfettiView konfettiView = findViewById(R.id.konfettiView);

        konfettiView.build().addColors(
                Color.parseColor("#F2C14E"),
                Color.parseColor("#E27D60"),
                Color.parseColor("#85D6FF"),
                Color.parseColor("#C38DFF"),
                Color.parseColor("#FF8FB1"),
                Color.parseColor("#F7F7F7"),
                Color.parseColor("#7DD87D"))
                .setPosition(0f, (float) getResources().getDisplayMetrics().widthPixels, -50f, -50f)
                .setDirection(70.0, 110.0)
                .setSpeed(0.5f, 2.5f)
                .addSizes(new Size(10, 3f), new Size(14, 4f), new Size(18, 5f))
                .addShapes(Shape.Square.INSTANCE)
                .setFadeOutEnabled(true)
                .setTimeToLive(6000L)
                .streamFor(45, 5000L);

        LinearLayout providersRow = findViewById(R.id.providersRow);

        if (movieId != null && !movieId.isEmpty()) {

            ApiService api = ApiClient.getInstance(this).create(ApiService.class);

            api.getProviders(Integer.parseInt(movieId)).enqueue(new Callback<Map<String, Object>>() {

                @Override @SuppressWarnings("unchecked")
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {

                    if (!response.isSuccessful() || response.body() == null) return;

                    Map<String, Object> results = (Map<String, Object>) response.body().get("results");
                    if (results == null) return;

                    Map<String, Object> region = (Map<String, Object>) results.get("US");
                    if (region == null && !results.isEmpty()) region = (Map<String, Object>) results.values().iterator().next();
                    if (region == null) return;

                    List<Map<String, Object>> providers = (List<Map<String, Object>>) region.get("flatrate");
                    if (providers == null) providers = (List<Map<String, Object>>) region.get("rent");
                    if (providers == null) providers = (List<Map<String, Object>>) region.get("buy");
                    if (providers == null) return;

                    float density = getResources().getDisplayMetrics().density;
                    int logoPx = (int) (28 * density);
                    int padH = (int) (10 * density);
                    int padV = (int) (8 * density);
                    int marginPx = (int) (6 * density);
                    int textGap = (int) (6 * density);

                    List<Map<String, Object>> finalProviders = providers;

                    runOnUiThread(() -> {
                        for (Map<String, Object> provider : finalProviders) {

                            String logoPath = (String) provider.get("logo_path");
                            String name = (String) provider.get("provider_name");

                            if (logoPath == null) continue;

                            LinearLayout badge = new LinearLayout(MatchActivity.this);
                            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            badgeParams.setMargins(marginPx, 0, marginPx, 0);
                            badge.setLayoutParams(badgeParams);
                            badge.setOrientation(LinearLayout.HORIZONTAL);
                            badge.setGravity(android.view.Gravity.CENTER_VERTICAL);
                            badge.setBackgroundResource(R.drawable.provider_badge_bg);
                            badge.setPadding(padH, padV, padH, padV);

                            ImageView logo = new ImageView(MatchActivity.this);
                            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoPx, logoPx);
                            logoParams.setMargins(0, 0, textGap, 0);
                            logo.setLayoutParams(logoParams);
                            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            Glide.with(MatchActivity.this).load("https://image.tmdb.org/t/p/w92" + logoPath).centerCrop().into(logo);

                            TextView nameView = new TextView(MatchActivity.this);
                            nameView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            nameView.setText(name);
                            nameView.setTextColor(android.graphics.Color.WHITE);
                            nameView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
                            nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                            badge.addView(logo);
                            badge.addView(nameView);
                            providersRow.addView(badge);

                        }

                    });

                }

                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {}

            });

        }

    }

}