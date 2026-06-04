package com.example.moviedates.network.model;

public class SoloSwipeRequest {
    private int movieId;
    private boolean accepted;

    public SoloSwipeRequest(int movieId, boolean accepted) {
        this.movieId = movieId;
        this.accepted = accepted;
    }

    public int getMovieId() { return movieId; }
    public boolean isAccepted() { return accepted; }
}