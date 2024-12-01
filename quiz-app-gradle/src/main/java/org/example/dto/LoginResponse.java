package org.example.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String message;
    private long expiresIn;  // Token expiration time in seconds
    private boolean success;

    // Constructor for successful login
    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
        this.message = "Login successful";
        this.expiresIn = 3600; // 1 hour by default
        this.success = true;
    }

    // Static factory method for error response
    public static LoginResponse error(String message) {
        LoginResponse response = new LoginResponse();
        response.setMessage(message);
        response.setToken("");
        response.setUsername("");
        response.setExpiresIn(3600);
        response.setSuccess(false);
        return response;
    }
}
