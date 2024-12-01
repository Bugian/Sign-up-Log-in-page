package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private boolean success;
    private String message;
    private String username;

    public static SignupResponse success(String username) {
        return new SignupResponse(true, "User registered successfully", username);
    }

    public static SignupResponse error(String message) {
        return new SignupResponse(false, message, null);
    }
}
