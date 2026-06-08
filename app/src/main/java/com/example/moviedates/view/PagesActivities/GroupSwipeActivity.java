package com.example.moviedates.view.PagesActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.MovieDTO;
import com.example.moviedates.network.model.VoteRequest;
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
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupSwipeActivity extends AppCompatActivity {

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private CardStackView cardStackView;
    private CardStackLayoutManager layoutManager;
    private ImageView backCardPoster;
    private MovieAdapter adapter;

    private final List<MovieDTO> movies = new ArrayList<>();

    private String roomCode;
    private long userId;
    private long sessionId;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_swipe);

        TextView mehButton = findViewById(R.id.mehButton);
        TextView loveButton = findViewById(R.id.loveButton);

        userId = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getLong("user_id", -1);
        sessionId = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getLong("session_id", -1);
        roomCode  = getIntent().getStringExtra("room_code");

        if (roomCode == null) {
            roomCode = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getString("room_code", "");
        }

        if (roomCode.isEmpty() || userId == -1 || sessionId == -1) {
            Toast.makeText(this, "Session data missing. Please rejoin the room.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cardStackView = findViewById(R.id.cardStackView);
        backCardPoster = findViewById(R.id.backCardPoster);

        setupCardStack();
        loadDeck();

        mehButton.setOnClickListener(v -> swipe(Direction.Left));
        loveButton.setOnClickListener(v -> swipe(Direction.Right));
    }

    private void setupCardStack() {

        layoutManager = new CardStackLayoutManager(this, new CardStackListener() {

            @Override
            public void onCardDragging(Direction direction, float ratio) {}

            @Override
            public void onCardSwiped(Direction direction) {

                boolean liked = direction == Direction.Right;
                submitVote(movies.get(currentPosition), liked);

                currentPosition = layoutManager.getTopPosition();
                int backIndex = currentPosition + 1;

                if (backIndex < movies.size()) {
                    backCardPoster.setVisibility(View.VISIBLE);
                    loadPoster(movies.get(backIndex).getPosterPath(), backCardPoster);
                } else {
                    backCardPoster.setVisibility(View.INVISIBLE);
                }
            }

            @Override public void onCardRewound() {}
            @Override public void onCardCanceled() {}
            @Override public void onCardAppeared(View view, int position) {}
            @Override public void onCardDisappeared(View view, int position) {}

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

        adapter = new MovieAdapter(movies);
        cardStackView.setAdapter(adapter);
    }

    private void loadDeck() {

        ApiClient.getInstance(this).create(ApiService.class).getDeck(roomCode)
                .enqueue(new Callback<List<MovieDTO>>() {

                    @SuppressLint("NotifyDataSetChanged") @Override
                    public void onResponse(@NonNull Call<List<MovieDTO>> call, @NonNull Response<List<MovieDTO>> response) {
                        if (isDestroyed() || isFinishing()) return;

                        if (response.isSuccessful() && response.body() != null) {

                            movies.clear();
                            for (MovieDTO movie : response.body()) {
                                if (movie != null) movies.add(movie);
                            }

                            if (movies.isEmpty()) {
                                Toast.makeText(GroupSwipeActivity.this, "No movies available for this session.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            adapter.notifyDataSetChanged();
                            currentPosition = 0;

                            if (movies.size() > 1) {
                                backCardPoster.setVisibility(View.VISIBLE);
                                loadPoster(movies.get(1).getPosterPath(), backCardPoster);
                            } else {
                                backCardPoster.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            Toast.makeText(GroupSwipeActivity.this, "Failed to load movies (" + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<MovieDTO>> call, @NonNull Throwable t) {
                        if (isDestroyed() || isFinishing()) return;
                        Toast.makeText(GroupSwipeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void submitVote(MovieDTO movie, boolean liked) {

        VoteRequest request = new VoteRequest(sessionId, userId, movie.getId(), liked);

        ApiClient.getInstance(this).create(ApiService.class).submitVote(request)
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                        if (isDestroyed() || isFinishing()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> body = response.body();

                            String status = body.get("status") != null ? Objects.requireNonNull(body.get("status")).toString() : null;

                            if ("MATCHED".equals(status) || "finished".equals(status)) {

                                Object movieDetailsObj = body.get("movieDetails");
                                String title = null;
                                String posterPath = null;
                                String matchedId = null;

                                if (movieDetailsObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> details = (Map<String, Object>) movieDetailsObj;
                                    title = details.get("title") != null ? Objects.requireNonNull(details.get("title")).toString() : null;
                                    posterPath = details.get("posterPath") != null ? Objects.requireNonNull(details.get("posterPath")).toString() : null;
                                    matchedId  = details.get("id") != null ? Objects.requireNonNull(details.get("id")).toString() : null;
                                }

                                if (title == null) title = movie.getTitle();
                                if (posterPath == null) posterPath = movie.getPosterPath();

                                navigateToMatch(matchedId, title, posterPath);

                            } else {

                                Object matched = body.get("matched");
                                Object matchedMovieId = body.get("matchedMovieId");

                                if (Boolean.TRUE.equals(matched) && matchedMovieId != null) {
                                    navigateToMatch(matchedMovieId.toString(), movie.getTitle(), movie.getPosterPath());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {}
                });
    }

    private void navigateToMatch(String movieId, String title, String posterPath) {

        Intent intent = new Intent(this, MatchActivity.class);
        intent.putExtra("movie_id", movieId);
        intent.putExtra("movie_title", title);
        intent.putExtra("movie_poster", posterPath);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void swipe(Direction direction) {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder().setDirection(direction).setDuration(Duration.Normal.duration).build();
        layoutManager.setSwipeAnimationSetting(setting);
        cardStackView.swipe();
    }

    private void loadPoster(String posterPath, ImageView target) {

        if (posterPath == null || posterPath.isEmpty()) {
            target.setImageResource(R.drawable.genre_action);
            return;
        }

        String url = posterPath.startsWith("http") ? posterPath : TMDB_IMAGE_BASE + posterPath;
        Glide.with(this).load(url).centerCrop().placeholder(R.drawable.genre_action).into(target);

    }

    class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        private final List<MovieDTO> movieList;

        MovieAdapter(List<MovieDTO> movieList) { this.movieList = movieList; }

        @NonNull @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_card2, parent, false);
            return new MovieViewHolder(view);
        }

        @SuppressLint("SetTextI18n") @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            MovieDTO movie = movieList.get(position);
            loadPoster(movie.getPosterPath(), holder.moviePoster);
            holder.movieTitle.setText(movie.getTitle());
            String year = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ? movie.getReleaseDate().substring(0, 4) : "";
            holder.movieInfo.setText(year);
            holder.movieGenres.setText("");
            int ratingPct = (int) Math.round(movie.getVoteAverage() * 10);
            holder.ratingProgress.setProgress(ratingPct);
            holder.ratingText.setText(ratingPct + "%");
        }

        @Override
        public int getItemCount() { return movieList.size(); }

        class MovieViewHolder extends RecyclerView.ViewHolder {

            ImageView moviePoster;
            TextView movieTitle, movieInfo, movieGenres, ratingText;
            CircularProgressIndicator ratingProgress;

            MovieViewHolder(@NonNull View itemView) {

                super(itemView);

                moviePoster = itemView.findViewById(R.id.moviePoster);
                movieTitle = itemView.findViewById(R.id.movieTitle);
                movieInfo = itemView.findViewById(R.id.movieInfo);
                movieGenres = itemView.findViewById(R.id.movieGenres);
                ratingText = itemView.findViewById(R.id.ratingText);
                ratingProgress = itemView.findViewById(R.id.ratingProgress);

            }

        }

    }
}