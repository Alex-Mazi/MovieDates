package com.example.moviedates.view.PagesActivities;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.match);

        KonfettiView konfettiView = findViewById(R.id.konfettiView);

        konfettiView.build()

                .addColors(
                        Color.parseColor("#F2C14E"), // warm gold
                        Color.parseColor("#E27D60"), // coral
                        Color.parseColor("#85D6FF"), // soft blue
                        Color.parseColor("#C38DFF"), // lavender
                        Color.parseColor("#FF8FB1"), // pink
                        Color.parseColor("#F7F7F7"), // white
                        Color.parseColor("#7DD87D")  // muted mint
                )

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