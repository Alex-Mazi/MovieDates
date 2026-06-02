package com.example.moviedates.view.PagesActivities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviedates.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupSwipeActivity extends AppCompatActivity {

    private CardStackView cardStackView;

    private CardStackLayoutManager layoutManager;

    private ImageView backCardPoster;

    private final List<MovieModel> movies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_swipe);

        cardStackView = findViewById(R.id.cardStackView);

        backCardPoster = findViewById(R.id.backCardPoster);

        TextView mehButton = findViewById(R.id.mehButton);

        TextView loveButton = findViewById(R.id.loveButton);

        loadMovies();

        // INITIAL BACK CARD
        if (movies.size() > 1) {

            backCardPoster.setImageResource(movies.get(1).poster);
        }

        // CARD STACK
        layoutManager = new CardStackLayoutManager(this,
                new CardStackListener() {

                    @Override
                    public void onCardDragging(Direction direction, float ratio) {}

                    @Override
                    public void onCardSwiped(Direction direction) {

                        int topPosition = layoutManager.getTopPosition();

                        int backIndex = topPosition + 1;

                        // UPDATE BACK CARD
                        if (backIndex < movies.size()) {

                            backCardPoster.setVisibility(View.VISIBLE);

                            backCardPoster.setImageResource(movies.get(backIndex).poster);

                        } else {

                            backCardPoster.setVisibility(View.INVISIBLE);
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

        layoutManager.setCanScrollVertical(false);

        layoutManager.setDirections(Arrays.asList(Direction.Left, Direction.Right));

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

    // LOAD DATA
    private void loadMovies() {

        movies.add(new MovieModel(
                R.drawable.p33156_p_v8_aq1,
                "The Lord Of The Rings: The Return Of The King",
                "2003",
                "201min",
                "Adventure, Fantasy, Action",
                85
        ));

        movies.add(new MovieModel(
                R.drawable.genre_action,
                "Mad Max: Fury Road",
                "2015",
                "120min",
                "Action, Adventure",
                97
        ));

        movies.add(new MovieModel(
                R.drawable.genre_comedy,
                "The Hangover",
                "2009",
                "100min",
                "Comedy",
                78
        ));

        movies.add(new MovieModel(
                R.drawable.genre_horror,
                "The Conjuring",
                "2013",
                "112min",
                "Horror, Mystery",
                84
        ));
    }

    // MODEL
    static class MovieModel {

        int poster;

        String title;

        String year;

        String runtime;

        String genres;

        int rating;

        MovieModel(int poster, String title, String year, String runtime, String genres, int rating) {

            this.poster = poster;

            this.title = title;

            this.year = year;

            this.runtime = runtime;

            this.genres = genres;

            this.rating = rating;
        }
    }

    // ADAPTER
    static class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        private final List<MovieModel> movieList;

        MovieAdapter(List<MovieModel> movieList) {this.movieList = movieList;}

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_card2, parent, false);

            return new MovieViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {

            MovieModel movie = movieList.get(position);

            holder.moviePoster.setImageResource(movie.poster);

            holder.movieTitle.setText(movie.title);

            holder.movieInfo.setText(movie.year + ", " + movie.runtime);

            holder.movieGenres.setText(movie.genres);

            // RATING
            holder.ratingProgress.setProgress(movie.rating);

            holder.ratingText.setText(movie.rating + "%");
        }

        @Override
        public int getItemCount() {
            return movieList.size();
        }

        // VIEW HOLDER
        static class MovieViewHolder extends RecyclerView.ViewHolder {

            ImageView moviePoster;

            TextView movieTitle;

            TextView movieInfo;

            TextView movieGenres;

            TextView ratingText;

            CircularProgressIndicator ratingProgress;

            MaterialCardView cardView;

            MovieViewHolder(@NonNull View itemView) {

                super(itemView);

                moviePoster = itemView.findViewById(R.id.moviePoster);

                movieTitle = itemView.findViewById(R.id.movieTitle);

                movieInfo = itemView.findViewById(R.id.movieInfo);

                movieGenres = itemView.findViewById(R.id.movieGenres);

                ratingText = itemView.findViewById(R.id.ratingText);

                ratingProgress = itemView.findViewById(R.id.ratingProgress);

                cardView = (MaterialCardView) itemView;
            }
        }
    }
}