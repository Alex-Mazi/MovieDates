package com.example.moviedates.network.model;

/**
 * Response body from POST /api/auth/check-email
 *
 * Spring Boot returns:  { "exists": true/false }
 */
public class CheckEmailResponse {

    private boolean exists;   // true  → email already registered  → go to Login
    // false → new user                  → go to SignUp

    public CheckEmailResponse() {}

    public CheckEmailResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }
}