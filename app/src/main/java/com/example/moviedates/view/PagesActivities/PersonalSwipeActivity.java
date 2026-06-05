package com.example.moviedates.view.PagesActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.moviedates.R;
import com.example.moviedates.network.ApiClient;
import com.example.moviedates.network.ApiService;
import com.example.moviedates.network.model.MovieDTO;
import com.example.moviedates.network.model.SoloSwipeRequest;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalSwipeActivity extends AppCompatActivity {

    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    private CardStackView cardStackView;
    private ImageView backCardPoster;
    private MovieAdapter adapter;

    private final List<MovieDTO> movies = new ArrayList<>();
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_swipe);

        cardStackView = findViewById(R.id.cardStackView);
        backCardPoster = findViewById(R.id.backCardPoster);
        TextView mehButton = findViewById(R.id.mehButton);
        TextView loveButton = findViewById(R.id.loveButton);

        userId = getSharedPreferences("moviedates_prefs", MODE_PRIVATE).getLong("user_id", -1);

        setupCardStack();
        fetchSoloDeck();

        mehButton.setOnClickListener(v -> swipe(Direction.Left));
        loveButton.setOnClickListener(v -> swipe(Direction.Right));

    }

    private void setupCardStack() {

        CardStackLayoutManager layoutManager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) { }

            @Override
            public void onCardSwiped(Direction direction) {

                int swipedPosition = layoutManager().getTopPosition() - 1;

                if (swipedPosition >= 0 && swipedPosition < movies.size()) {
                    MovieDTO swipedMovie = movies.get(swipedPosition);
                    boolean accepted = direction == Direction.Right;
                    postSoloSwipe(swipedMovie.getId(), accepted);
                }

                int nextBack = layoutManager().getTopPosition() + 1;

                if (nextBack < movies.size()) {
                    backCardPoster.setVisibility(View.VISIBLE);
                    Glide.with(PersonalSwipeActivity.this).load(TMDB_IMAGE_BASE + movies.get(nextBack).getPosterPath()).placeholder(new ColorDrawable(Color.TRANSPARENT)).transition(DrawableTransitionOptions.withCrossFade()).into(backCardPoster);
                } else {
                    backCardPoster.setVisibility(View.INVISIBLE);
                }

                if (layoutManager().getTopPosition() == movies.size()) { showDoneDialog(); }
            }

            @Override public void onCardRewound() { }
            @Override public void onCardCanceled() { }
            @Override public void onCardAppeared(View view, int position) { }
            @Override public void onCardDisappeared(View view, int position) { }
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
        adapter = new MovieAdapter(movies);
        cardStackView.setAdapter(adapter);
    }

    private CardStackLayoutManager layoutManager() { return (CardStackLayoutManager) cardStackView.getLayoutManager(); }

    private void fetchSoloDeck() {

        if (userId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiClient.getInstance(this).create(ApiService.class).getSoloDeck(userId)
                .enqueue(new Callback<Map<String, Object>>() {

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            List<?> raw = (List<?>) response.body().get("results");

                            if (raw != null) {

                                movies.clear();

                                for (Object item : raw) {
                                    if (item instanceof Map) {
                                        Map<?, ?> map = (Map<?, ?>) item;
                                        MovieDTO dto = new MovieDTO();
                                        dto.setId(((Double) map.get("id")).intValue());
                                        dto.setTitle((String) map.get("title"));
                                        dto.setPosterPath((String) map.get("poster_path"));
                                        movies.add(dto);
                                    }
                                }

                                adapter.notifyDataSetChanged();

                                if (movies.size() > 1) {
                                    backCardPoster.setVisibility(View.VISIBLE);
                                    Glide.with(PersonalSwipeActivity.this).load(TMDB_IMAGE_BASE + movies.get(1).getPosterPath()).placeholder(new ColorDrawable(Color.TRANSPARENT)).transition(DrawableTransitionOptions.withCrossFade()).into(backCardPoster);
                                }

                            }

                        } else {
                            Toast.makeText(PersonalSwipeActivity.this, "Failed to load movies", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) { Toast.makeText(PersonalSwipeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show(); }

                });
    }

    private void postSoloSwipe(int movieId, boolean accepted) {

        if (userId == -1) return;

        ApiClient.getInstance(this).create(ApiService.class).postSoloSwipe(userId, new SoloSwipeRequest(movieId, accepted))
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) { Log.w("PersonalSwipe", "Solo swipe post failed: " + response.code()); }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) { Log.e("PersonalSwipe", "Solo swipe error: " + t.getMessage()); }

                });

    }

    private void swipe(Direction direction) {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder().setDirection(direction).setDuration(Duration.Normal.duration).build();
        layoutManager().setSwipeAnimationSetting(setting);
        cardStackView.swipe();
    }

    private void showDoneDialog() {
        new AlertDialog.Builder(this).setTitle("Thank You").setMessage("Thank you for your input.").setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    startActivity(new Intent(PersonalSwipeActivity.this, RoomActivity.class));
                    finish();
                }).show();
    }

    static class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

        private final List<MovieDTO> movieList;

        MovieAdapter(List<MovieDTO> movieList) {
            this.movieList = movieList;
        }

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_card, parent, false);
            return new MovieViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            MovieDTO movie = movieList.get(position);
            holder.movieTitle.setText(movie.getTitle());
            Glide.with(holder.moviePoster.getContext()).load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath()).placeholder(new ColorDrawable(Color.TRANSPARENT)).transition(DrawableTransitionOptions.withCrossFade()).into(holder.moviePoster);
        }

        @Override
        public int getItemCount() { return movieList.size(); }

        static class MovieViewHolder extends RecyclerView.ViewHolder {
            ImageView moviePoster;
            TextView movieTitle;

            public MovieViewHolder(@NonNull View itemView) {
                super(itemView);
                moviePoster = itemView.findViewById(R.id.moviePoster);
                movieTitle = itemView.findViewById(R.id.movieTitle);
            }
        }
    }
}