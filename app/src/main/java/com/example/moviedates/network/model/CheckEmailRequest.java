// ============================================================
// FILE 1:  network/model/CheckEmailRequest.java
// ============================================================
package com.example.moviedates.network.model;

/**
 * Request body for POST /api/auth/check-email
 *
 * Spring Boot receives this as @RequestBody CheckEmailRequest
 */
public class CheckEmailRequest {

    private String email;

    public CheckEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}


// ============================================================
// FILE 2:  network/model/CheckEmailResponse.java
// ============================================================
//
// package com.example.moviedates.network.model;
//
// /**
//  * Response body from POST /api/auth/check-email
//  *
//  * Spring Boot returns:  { "exists": true/false }
//  */
// public class CheckEmailResponse {
//
//     private boolean exists;   // true  → email already registered  → go to Login
//                                // false → new user                  → go to SignUp
//
//     public CheckEmailResponse() {}
//     public CheckEmailResponse(boolean exists) { this.exists = exists; }
//
//     public boolean isExists() { return exists; }
//     public void setExists(boolean exists) { this.exists = exists; }
// }