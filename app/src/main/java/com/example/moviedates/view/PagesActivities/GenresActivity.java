package com.example.moviedates.view.PagesActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviedates.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class GenresActivity extends AppCompatActivity {

    private int selectedGenresCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genres);

        MaterialButton nextButton = findViewById(R.id.nextButton);

        nextButton.setAlpha(0.5f);

        View genreGrid = findViewById(R.id.genreGrid);

        // Setup all cards
        if (genreGrid instanceof GridLayout) {

            GridLayout grid = (GridLayout) genreGrid;

            for (int i = 0; i < grid.getChildCount(); i++) {

                View child = grid.getChildAt(i);

                if (child instanceof MaterialCardView) {setupGenreCard((MaterialCardView) child, nextButton);}
            }
        }

        nextButton.setOnClickListener(v -> {

            if (selectedGenresCount < 3) {

                Toast.makeText(GenresActivity.this, "Please select at least 3 genres", Toast.LENGTH_SHORT).show();

            } else {

                Intent intent = new Intent(GenresActivity.this, MatchActivity.class);

                startActivity(intent);

            }

        });
    }

    private void setupGenreCard(MaterialCardView card, MaterialButton nextButton) {

        card.setTag(false);

        // DEFAULT = NO BORDER
        card.setStrokeWidth(0);

        card.setOnClickListener(v -> {

            boolean selected = (boolean) card.getTag();

            selected = !selected;

            card.setTag(selected);

            if (selected) {

                selectedGenresCount++;

                card.setStrokeWidth(6);
                card.setStrokeColor(android.graphics.Color.WHITE);

                card.setCardElevation(14f);

                card.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).start();

                updateNextButton(nextButton);

            } else {

                selectedGenresCount--;

                // UNSELECTED STATE
                card.setStrokeWidth(0);

                card.setCardElevation(6f);

                card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();

                updateNextButton(nextButton);

            }

        });
    }

    private void updateNextButton(MaterialButton nextButton) {

        float alpha;

        switch (selectedGenresCount) {
            case 0:
                alpha = 0.35f;
                break;
            case 1:
                alpha = 0.55f;
                break;
            case 2:
                alpha = 0.75f;
                break;
            default:
                alpha = 1.0f;
                break;
        }

        nextButton.animate().alpha(alpha).setDuration(150).start();
    }

}