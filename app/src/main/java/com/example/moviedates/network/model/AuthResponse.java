package com.example.moviedates.network.model;

public class AuthResponse {
    private String token;
    private UserPayload user;

    public String getToken() { return token; }
    public UserPayload getUser() { return user; }

    public static class UserPayload {
        private long id;
        private String email;
        private String displayName;

        public long getId() { return id; }
        public String getEmail() { return email; }
        public String getDisplayName() { return displayName; }
    }
}