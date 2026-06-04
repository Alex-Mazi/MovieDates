package com.example.moviedates.network.model;

import java.util.List;

public class SessionResponse {
    private Long id;
    private String code;
    private boolean active;
    private boolean finished;
    private Integer matchedMovieId;
    private List<AuthResponse.UserPayload> participants;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public boolean isActive() { return active; }
    public boolean isFinished() { return finished; }
    public Integer getMatchedMovieId() { return matchedMovieId; }
    public List<AuthResponse.UserPayload> getParticipants() { return participants; }
}