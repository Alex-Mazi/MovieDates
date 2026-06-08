package com.example.moviedates.view.PagesActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moviedates.R;

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
    }
}