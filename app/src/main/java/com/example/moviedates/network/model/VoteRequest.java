package com.example.moviedates.network.model;

public class VoteRequest {
    private Long sessionId;
    private Long userId;
    private Integer movieId;
    private boolean accepted;

    public VoteRequest(Long sessionId, Long userId, Integer movieId, boolean accepted) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.movieId = movieId;
        this.accepted = accepted;
    }

    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public Integer getMovieId() { return movieId; }
    public boolean isAccepted() { return accepted; }
}