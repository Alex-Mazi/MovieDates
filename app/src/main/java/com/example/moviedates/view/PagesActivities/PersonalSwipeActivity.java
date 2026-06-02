package com.example.moviedates.view.PagesActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviedates.R;
import com.google.android.material.card.MaterialCardView;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonalSwipeActivity extends AppCompatActivity {

    private CardStackView cardStackView;
    private CardStackLayoutManager layoutManager;
    private ImageView backCardPoster;

    private final List<MovieModel> movies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_swipe);

        cardStackView = findViewById(R.id.cardStackView);
        backCardPoster = findViewById(R.id.backCardPoster);

        TextView mehButton = findViewById(R.id.mehButton);
        TextView loveButton = findViewById(R.id.loveButton);

        loadMovies();

        if (movies.size() > 1) {backCardPoster.setImageResource(movies.get(1).poster);}

        layoutManager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {}

            @Override
            public void onCardSwiped(Direction direction) {

                if (direction == Direction.Left) {
                    // Meh
                } else if (direction == Direction.Right) {
                    // Loved
                } else if (direction == Direction.Top) {
                    // Skipped
                }

                int topPosition = layoutManager.getTopPosition();

                int backIndex = topPosition + 1;

                if (backIndex < movies.size()) {

                    backCardPoster.setVisibility(View.VISIBLE);

                    backCardPoster.setImageResource(movies.get(backIndex).poster);

                } else {

                    backCardPoster.setVisibility(View.INVISIBLE);
                }

                if (topPosition == movies.size()) {

                    new AlertDialog.Builder(PersonalSwipeActivity.this).setTitle("Thank You").setMessage("Thank you for your input.").setCancelable(false)
                            .setPositiveButton("Continue", (dialog, which) -> {
                                // Move to RoomActivity
                                Intent intent = new Intent(PersonalSwipeActivity.this, RoomActivity.class);
                                startActivity(intent);
                                finish();
                            }).show();
                }
            }

            @Override
            public void onCardRewound() {}

            @Override
            public void onCardCanceled() {}

            @Override
            public void onCardAppeared(View view, int position) {}

            @Override
            public void onCardDisappeared(View view, int position) {}
        });

        layoutManager.setStackFrom(StackFrom.None);
        layoutManager.setVisibleCount(1);
        layoutManager.setScaleInterval(0.0f);
        layoutManager.setTranslationInterval(0.0f);
        layoutManager.setMaxDegree(20.0f);
        layoutManager.setSwipeThreshold(0.3f);
        layoutManager.setCanScrollHorizontal(true);
        layoutManager.setCanScrollVertical(true);
        layoutManager.setDirections(Arrays.asList(Direction.Left, Direction.Right, Direction.Top));

        cardStackView.setLayoutManager(layoutManager);

        // ADAPTER
        MovieAdapter adapter = new MovieAdapter(movies);
        cardStackView.setAdapter(adapter);

        // LEFT BUTTON
        mehButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder().setDirection(Direction.Left).setDuration(Duration.Normal.duration).build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });

        // RIGHT BUTTON
        loveButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder().setDirection(Direction.Right).setDuration(Duration.Normal.duration).build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });
    }

    private void loadMovies() {
        movies.add(new MovieModel(R.drawable.p33156_p_v8_aq1, "The Lord Of The Rings: The Return Of The King"));
        movies.add(new MovieModel(R.drawable.genre_action, "Mad Max: Fury Road"));
        movies.add(new MovieModel(R.drawable.genre_comedy, "The Hangover"));
        movies.add(new MovieModel(R.drawable.genre_horror, "The Conjuring"));
    }

    static class MovieModel {
        int poster;
        String title;

        MovieModel(int poster, String title) {
            this.poster = poster;
            this.title = title;
        }
    }

    // ADAPTER
    static class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        List<MovieModel> movieList;

        MovieAdapter(List<MovieModel> movieList) { this.movieList = movieList; }

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_card, parent, false);
            return new MovieViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            MovieModel movie = movieList.get(position);
            holder.moviePoster.setImageResource(movie.poster);
            holder.movieTitle.setText(movie.title);
        }

        @Override
        public int getItemCount() { return movieList.size(); }

        static class MovieViewHolder extends RecyclerView.ViewHolder {
            ImageView moviePoster;
            TextView movieTitle;
            MaterialCardView cardView;

            public MovieViewHolder(@NonNull View itemView) {
                super(itemView);
                moviePoster = itemView.findViewById(R.id.moviePoster);
                movieTitle = itemView.findViewById(R.id.movieTitle);
                cardView = (MaterialCardView) itemView;
            }

        }

    }

}